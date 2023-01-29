package com.domainsurvey.crawler.service.crawler.store.impl;

import com.domainsurvey.crawler.model.domain.Domain;
import com.domainsurvey.crawler.model.domain.DomainCrawlingInfo;
import com.domainsurvey.crawler.model.link.Node;
import com.domainsurvey.crawler.model.page.Page;
import com.domainsurvey.crawler.model.type.NodeType;
import com.domainsurvey.crawler.service.crawler.importer.model.ImportStore;
import com.domainsurvey.crawler.service.crawler.model.PageResult;
import com.domainsurvey.crawler.service.crawler.processor.CrawlingProcessorService;
import com.domainsurvey.crawler.service.crawler.store.CrawlingStoreProcessor;
import com.domainsurvey.crawler.service.crawler.store.model.CrawlingStore;
import com.domainsurvey.crawler.service.crawler.store.model.StateStore;
import com.domainsurvey.crawler.service.dao.DomainCrawlingInfoService;
import com.domainsurvey.crawler.service.dao.QueryExecutor;
import com.domainsurvey.crawler.service.dao.page.ProgressPageService;
import com.domainsurvey.crawler.service.fetcher.FetcherProcessor;
import com.domainsurvey.crawler.service.table.TableService;
import com.domainsurvey.crawler.service.table.type.SchemaType;
import com.domainsurvey.crawler.service.table.type.TableType;
import com.domainsurvey.crawler.utils.Utils;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.domainsurvey.crawler.utils.Utils.getCurrentTimestamp;

@Service
@Scope("prototype")
@Log4j2
public class CrawlingStoreProcessorImpl implements CrawlingStoreProcessor {

    @Autowired
    private ProgressPageService progressPageService;
    @Autowired
    private QueryExecutor queryExecutor;
    @Autowired
    private FetcherProcessor fetcherProcessor;
    @Autowired
    private DomainCrawlingInfoService domainCrawlingInfoService;

    private Domain domain;

    public final StateStore stateStore;
    public CrawlingStore crawlingStore;
    private DomainCrawlingInfo crawlingInfo;
    private ImportStore importStore;
    protected final Object importCrawlerResultLock = new Object();

    public CrawlingStoreProcessorImpl(CrawlingProcessorService crawlingProcessorService, StateStore stateStore) {
        this.domain = crawlingProcessorService.domain();
        this.stateStore = stateStore;
        this.crawlingInfo = domain.getProcessDomainCrawlingInfo();
        this.crawlingStore = crawlingProcessorService.crawlingStore();
        this.importStore = crawlingProcessorService.importStore();
    }

    public void initQueue() {
        log.info("initQueue: {}", domain.getId());

        long finished = progressPageService.countTotal(domain);
        long finishedPage = progressPageService.countInternalTotal(domain);
        stateStore.finished = finished;
        stateStore.total = finished;

        stateStore.finishedPage = finishedPage;
        stateStore.totalPage = finishedPage;

        if (finished == 0) {
            List<Node> nodesToQueue = new ArrayList<>();

            Node nodeToProcess = createFirstLinkToProcess(domain.getUrl());

            importStore.addLink(nodeToProcess);
            nodesToQueue.add(nodeToProcess);

            log.info("addNodesToQueue on start: {}", domain.getId());

            addNodesToQueue(nodesToQueue);

            crawlingInfo.setCrawlingStartedTimestamp(getCurrentTimestamp());

            domainCrawlingInfoService.save(crawlingInfo);
        } else {
            initQueueFromDB();
        }

        if (crawlingStore.queueSize() > 1) {
            initAlreadyQueued();
        }

        this.stateStore.crawlingStartedTimestamp = this.crawlingInfo.getCrawlingStartedTimestamp();
    }

    private void initQueueFromDB() {
        log.info("initQueueFromDB : {}", domain.getId());

        String page = TableService.getFullTableName(domain.getId(), SchemaType.PROCESS, TableType.PAGE);
        String link = TableService.getFullTableName(domain.getId(), SchemaType.PROCESS, TableType.NODE);

        String template = "select l.* from %s l " +
                "left join %s p on p.id = l.id " +
                "where l.type = %s and p.id isnull";

        String sql = String.format(template, link, page, NodeType.INTERNAL.getValue());

        List<Node> result = getPagesForQueueBySql(sql);

        addNodesToQueue(result);
    }

    private void initAlreadyQueued() {
        log.info("initAlreadyQueued : {}", domain.getId());

        String from = TableService.getFullTableName(domain.getId(), SchemaType.PROCESS, TableType.NODE);

        String sql = String.format("select id from %s", from);

        List<Long> result = queryExecutor.queryForList(sql, Long.class);

        crawlingStore.alreadyQueuedAdd(result);

        importStore.alreadyImporter.addAll(result);
    }

    public Node createFirstLinkToProcess(String domainUrl) {
        String validUrl = domainUrl;
        try {
            validUrl = fetcherProcessor.getValidFirstDomainUrlWithSlash(domainUrl, false);
        } catch (Exception ignored) {
        }

        log.info("first valid url: " + validUrl);

        return Node.builder()
                .url(validUrl)
                .id(Utils.getCRC32(validUrl))
                .type(NodeType.INTERNAL)
                .depth((short) 0)
                .redirectCount((byte) 0)
                .robotsValid(true)
                .redirectedLinks(new ArrayList<>())
                .build();
    }

    private List<Node> getPagesFromLastTableForCrawling() {
        String pageTable = TableService.getFullTableName(domain.getId(), SchemaType.FINAL, TableType.PAGE);
        String linkTable = TableService.getFullTableName(domain.getId(), SchemaType.FINAL, TableType.PAGE);

        String sql =
                "SELECT " +
                        "    l.* " +
                        "   FROM %s l" +
                        "   LEFT JOIN %s p on l.id = p.id" +
                        "   where l.type = %s";

        sql = String.format(sql, NodeType.INTERNAL.getValue(), linkTable, pageTable);

        return getPagesForQueueBySql(sql);
    }

    private List<Node> getPagesForQueueBySql(String sql) {
        return queryExecutor.queryList(sql, (rs, var1) -> Node.builder()
                .id(rs.getLong("id"))
                .type(NodeType.INTERNAL)
                .url(rs.getString("url"))
                .depth(rs.getByte("depth"))
                .redirectCount(rs.getByte("redirect_count"))
                .build());
    }

    @SneakyThrows
    public synchronized void addNodesToQueue(List<Node> nodeToQueue) {
        if (nodeToQueue.size() > 0) {
            log.info("addNodesToQueue {} {}: " + domain.getId(), nodeToQueue.size());
        }

        long addToQueueLimit = domain.getConfig().getPagesLimit() - stateStore.totalPage;
        int queued = 0;
        int queuedPage = 0;
        for (Node nodeToProcess : nodeToQueue) {
            long id = nodeToProcess.getId();
            if (!nodeToProcess.getType().equals(NodeType.INTERNAL) || queued < addToQueueLimit) {
                boolean pushed = crawlingStore.alreadyQueuedAdd(id);

                if (pushed) {
                    crawlingStore.putToQueue(nodeToProcess);
                    queued++;
                    if (nodeToProcess.getType().equals(NodeType.INTERNAL)) {
                        queuedPage++;
                    }
                }
            }
        }

        addQueued(queued, queuedPage);
    }

    public void processCrawlerResult(PageResult pageResult) {
        synchronized (importCrawlerResultLock) {
            List<Node> nodes = new ArrayList<>();
            Page pageToProcess = pageResult.toPage();

            importStore.addPage(pageToProcess);
            importStore.edges.addAll(pageResult.getEdges());

            for (Node node : pageResult.getNodes()) {
                importStore.addLink(node);

                if (shouldAddToQueue(node)) {
                    if (!crawlingStore.alreadyQueuedContains(node.getId())) {
                        nodes.add(node);
                    }
                }
            }
            if (!nodes.isEmpty()) {
                addNodesToQueue(nodes);
            }
        }
    }

    public boolean shouldAddToQueue(Node node) {
        return node.getType().equals(NodeType.INTERNAL)/* || node.getType().equals(NodeType.EXTERNAL)*/;
    }

    private void addQueued(int queuedCount, int queuePageCount) {
        if (queuedCount == 0) {
            return;
        }
        synchronized (stateStore) {
            stateStore.total += queuedCount;
            stateStore.queue += queuedCount;

            stateStore.totalPage += queuePageCount;
            stateStore.queuePage += queuePageCount;

            log.debug(
                    "{} total: {}: inQueue: {}",
                    domain.getId(), stateStore.total, stateStore.queue
            );
        }
    }
}