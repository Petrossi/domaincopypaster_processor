package com.domainsurvey.crawler.service.crawler.finalizer.impl.processor;

import com.domainsurvey.crawler.model.domain.Domain;
import com.domainsurvey.crawler.model.link.Node;
import com.domainsurvey.crawler.model.type.DownloadStatus;
import com.domainsurvey.crawler.model.type.FinalizerStatus;
import com.domainsurvey.crawler.model.type.NodeType;
import com.domainsurvey.crawler.service.PageCacheManager;
import com.domainsurvey.crawler.service.crawler.finalizer.CrawlingFinalizerProcessor;
import com.domainsurvey.crawler.service.dao.QueryExecutor;
import com.domainsurvey.crawler.service.fetcher.Downloader;
import com.domainsurvey.crawler.service.table.TableService;
import com.domainsurvey.crawler.service.table.type.SchemaType;
import com.domainsurvey.crawler.service.table.type.TableType;
import com.domainsurvey.crawler.utils.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Log4j2
public class ResourceDownloaderProcessor implements CrawlingFinalizerProcessor {
    @Autowired
    protected QueryExecutor queryExecutor;

    @Autowired
    private PageCacheManager pageCacheManager;

    public void process(Domain domain) {
        log.info("start: " + domain.getId());

        var next = getNext(domain);

        while (next.isPresent()) {
            var node = next.get();

            log.info("download: " + node.getId());

            downloadAndSave(domain, node);

            next = getNext(domain);
        }

        log.info("finished : " + domain.getId());
    }

    public void downloadAndSave(Domain domain, Node node) {
        var filePath = pageCacheManager.getFullPathForUrl(Utils.getCRC32(domain.getUrl()), node.getId());

        try {
            Downloader.downloadUsingStream(node.getUrl(), filePath);
            save(domain, node, DownloadStatus.SUCCESS);
        } catch (IOException e) {
            e.printStackTrace();

            save(domain, node, DownloadStatus.FAILED);
        }
    }

    private void save(Domain domain, Node node, DownloadStatus status) {
        String nodeTable = TableService.getFullTableName(domain.getId(), SchemaType.PROCESS, TableType.NODE);

        String sql = String.format("update %s set download_status = %s where id = %s", nodeTable, status.getValue(), node.getId());

        try {
            this.queryExecutor.executeQuery(sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Optional<Node> getNext(Domain domain) {
        String nodeTable = TableService.getFullTableName(domain.getId(), SchemaType.PROCESS, TableType.NODE);

        String sql = String.format(
                "SELECT id, url, type FROM %s p where download_status = %s and type in (%s, %s, %s) limit 1",
                nodeTable, DownloadStatus.PENDING, NodeType.CSS.getValue(), NodeType.IMAGE.getValue(), NodeType.JS.getValue()
        );

        try {
            var node = queryExecutor.queryForObject(sql, (rs, var1) -> Node.builder()
                    .id(rs.getLong("id"))
                    .url(rs.getString("url"))
                    .type(NodeType.fromValue(rs.getByte("type"))).build());

            return Optional.of(node);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public FinalizerStatus getNextStatus() {
        return FinalizerStatus.COUNT_PAGE_RANK;
    }
}