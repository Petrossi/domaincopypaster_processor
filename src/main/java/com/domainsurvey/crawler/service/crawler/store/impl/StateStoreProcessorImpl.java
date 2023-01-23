package com.domainsurvey.crawler.service.crawler.store.impl;

import com.domainsurvey.crawler.model.domain.Domain;
import com.domainsurvey.crawler.model.domain.DomainCrawlingInfo;
import com.domainsurvey.crawler.model.link.Node;
import com.domainsurvey.crawler.model.page.Page;
import com.domainsurvey.crawler.model.type.NodeType;
import com.domainsurvey.crawler.service.crawler.model.PageResult;
import com.domainsurvey.crawler.service.crawler.processor.CrawlingProcessorService;
import com.domainsurvey.crawler.service.crawler.store.CrawlingStoreProcessor;
import com.domainsurvey.crawler.service.crawler.store.StateStoreProcessor;
import com.domainsurvey.crawler.service.crawler.store.model.CrawlingStore;
import com.domainsurvey.crawler.service.crawler.store.model.StateStore;
import com.domainsurvey.crawler.service.dao.DomainCrawlingInfoService;
import com.domainsurvey.crawler.service.dao.page.ProgressPageService;
import com.domainsurvey.crawler.service.filter.FillFiltersService;
import com.domainsurvey.crawler.thread.CrawlingProgressUpdaterThread;
import com.domainsurvey.crawler.web.ws.publisher.ProgressPublisherService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.domainsurvey.crawler.service.filter.FilterParserService.ERROR_ISSUE_FILTER_ID;
import static com.domainsurvey.crawler.service.filter.FilterParserService.NOTICE_ISSUE_FILTER_ID;
import static com.domainsurvey.crawler.service.filter.FilterParserService.NO_ISSUE_FILTER_ID;
import static com.domainsurvey.crawler.service.filter.FilterParserService.WARNING_ISSUE_FILTER_ID;
import static com.domainsurvey.crawler.utils.Constants.BAD_STATUS_CODES;
import static com.domainsurvey.crawler.utils.Utils.sleepSeconds;

@Service
@Scope("prototype")
@Log4j2
public class StateStoreProcessorImpl implements StateStoreProcessor {

    @Autowired
    private DomainCrawlingInfoService domainCrawlingInfoService;
    @Autowired
    private ProgressPageService progressPageService;
    @Autowired
    private FillFiltersService fillFiltersService;
    @Autowired
    private ProgressPublisherService progressPublisherService;

    private Domain domain;

    private boolean isRunning = false;
    private boolean started = false;

    private DomainCrawlingInfo crawlingInfo;
    private CrawlingStoreProcessor crawlingStoreProcessor;

    boolean needsUpdateDomainCrawlingInfo = true;

    private CrawlingProcessorService crawlingProcessorService;
    public CrawlingStore crawlingStore;
    public final Object updaterLock = new Object();
    public final StateStore stateStore;

    public StateStoreProcessorImpl(CrawlingProcessorService crawlingProcessorService, StateStore stateStore) {
        this.crawlingProcessorService = crawlingProcessorService;
        this.stateStore = stateStore;
        this.domain = crawlingProcessorService.domain();
        this.crawlingInfo = domain.getProcessDomainCrawlingInfo();
        this.crawlingStoreProcessor = crawlingProcessorService.crawlingStoreProcessor();
        this.crawlingStore = crawlingProcessorService.crawlingStore();
    }

    private void initStateStore() {
        if (stateStore.total > 1) {
            stateStore.blocked = progressPageService.countBadStatusCode(domain);
            stateStore.error = 0;
            stateStore.warning = 0;
            stateStore.notice = 0;
            stateStore.noIssue = 0;
            stateStore.totalScore = progressPageService.countTotalScore(domain);
            stateStore.filtersEnabled = false;
        } else {
            stateStore.filtersEnabled = true;
        }

        stateStore.queue = crawlingStore.queueSize();
        stateStore.queuePage = crawlingStore.queuePageSize();
        stateStore.total = stateStore.queue + stateStore.finished;
        stateStore.totalPage = stateStore.queue + stateStore.finishedPage;
    }

    private void refreshCrawlingInfoFromStateStore() {
        synchronized (stateStore) {
            crawlingInfo.setTotal(stateStore.totalPage);
            crawlingInfo.setInQueue(stateStore.queue);
            crawlingInfo.setBlocked(stateStore.blocked);
            crawlingInfo.setError(stateStore.error);
            crawlingInfo.setWarning(stateStore.warning);
            crawlingInfo.setNotice(stateStore.notice);
            crawlingInfo.setNoIssue(stateStore.noIssue);
            crawlingInfo.setScore(stateStore.score);
        }
    }

    public void init() {
        isRunning = true;

        crawlingStoreProcessor.initQueue();
        initStateStore();

        refreshCrawlingInfoFromStateStore();
    }

    public void start() {
        if (started) {
            return;
        }
        started = true;
        new CrawlingProgressUpdaterThread(this).start();
    }

    public void stop() {
        isRunning = false;
        runDomainCrawlingInfoUpdate();
        stateStore.refreshIssuesFilters();

        fillFiltersService.savePreCountedFilters(stateStore.filters, domain);
    }

    public void run() {
        while (isRunning) {
            try {
                runCommonUpdate();
            } catch (Exception e) {
                e.printStackTrace();
                log.error(e.getMessage());
            }

            sleepSeconds(2);
        }
    }

    private void runCommonUpdate() {
        if (needsUpdateDomainCrawlingInfo) {
            synchronized (updaterLock) {
                refreshCrawlingInfoFromStateStore();
                runDomainCrawlingInfoUpdate();

                log.info(
                        "{}: total: {}/{} queue: {}/{} finished: {}/{} progress: {}",
                        domain.getId(), stateStore.total, stateStore.totalPage, stateStore.queue,
                        stateStore.queuePage, stateStore.finished, stateStore.finishedPage, stateStore.progress
                );

                log.info(
                        "{} error {} blocked: {} notice: {} warning: {} noIssue: {} score: {} : minutes: {}",
                        domain.getId(), stateStore.error, stateStore.blocked, stateStore.notice, stateStore.warning, stateStore.noIssue, crawlingInfo.getScore(), crawlingInfo.minutesFromStart()
                );

                progressPublisherService.publishProcessUpdate(domain.getId(), stateStore);

                if (crawlingInfo.minutesFromStart() > 100) {
                    crawlingProcessorService.stop(false);
                }
            }
        }
    }

    private void runDomainCrawlingInfoUpdate() {
        try {
            refreshCrawlingInfoFromStateStore();
            domainCrawlingInfoService.save(crawlingInfo);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
    }

    public void logCountersByImportedPages(List<Page> importedPages) {
        long errorCount = 0;
        long warningCount = 0;
        long noticeCount = 0;
        long blocked = 0;
        long noIssue = 0;
        double totalScore = 0;

        Map<Integer, Long> filtersCounters = new HashMap<>();

        for (Page page : importedPages) {
            if (BAD_STATUS_CODES.contains(page.getStatusCode())) {
                blocked++;
            }

            if (page.getFilters().contains(ERROR_ISSUE_FILTER_ID)) {
                errorCount++;
            }
            if (page.getFilters().contains(WARNING_ISSUE_FILTER_ID)) {
                warningCount++;
            }
            if (page.getFilters().contains(NOTICE_ISSUE_FILTER_ID)) {
                noticeCount++;
            }
            if (page.getFilters().contains(NO_ISSUE_FILTER_ID)) {
                noIssue++;
            }

            for (Integer filterId : page.getFilters()) {
                try {
                    filtersCounters.put(filterId, filtersCounters.containsKey(filterId) ? filtersCounters.get(filterId) + 1 : 1);
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("{} : {}", domain.getId(), e.getMessage());
                }
            }

            totalScore += page.getScore();
        }

        synchronized (stateStore) {
            stateStore.error += errorCount;
            stateStore.warning += warningCount;
            stateStore.notice += noticeCount;
            stateStore.blocked += blocked;
            stateStore.noIssue += noIssue;
            stateStore.totalScore += totalScore;
            stateStore.finishedImporting -= importedPages.size();
            stateStore.score = (byte) (stateStore.totalScore / stateStore.totalPage);

            for (Map.Entry<Integer, Long> filterCounter : filtersCounters.entrySet()) {
                long value = stateStore.filters.containsKey(filterCounter.getKey()) ? stateStore.filters.get(filterCounter.getKey()) + filterCounter.getValue() : filterCounter.getValue();

                stateStore.filters.put(filterCounter.getKey(), value);
            }

            log.debug(
                    "{} error: {} | warning: {} | notice: {} | blocked: {}| noIssue: {}",
                    domain.getId(), errorCount, warningCount, noticeCount, blocked, noIssue
            );
        }

        crawlingProcessorService.checkIfNeedFinish();
    }

    public void nodeStarted(Node nodeToProcess) {
        synchronized (stateStore) {
            stateStore.queue -= 1;
            if (nodeToProcess.getType().equals(NodeType.INTERNAL)) {
                stateStore.queuePage -= 1;
            }
            stateStore.progress += 1;
            stateStore.finishedImporting += 1;

            log.debug(
                    "{} inQueue: {}: inProgress: {}",
                    domain.getId(), stateStore.queue, stateStore.progress
            );
        }
    }

    public void pageFinished(PageResult pageResult) {
        synchronized (stateStore) {
            stateStore.progress -= 1;
            stateStore.finished += 1;
            try {
                if (pageResult.getNodeType().equals(NodeType.INTERNAL)) {
                    stateStore.finishedPage += 1;
                }
            } catch (Exception e) {
                e.getStackTrace();
                stateStore.finishedPage += 1;
            }

            stateStore.addCrawledPage(pageResult);
            log.debug(
                    "{} inProgress: {}",
                    domain.getId(), stateStore.progress
            );
        }
    }
}