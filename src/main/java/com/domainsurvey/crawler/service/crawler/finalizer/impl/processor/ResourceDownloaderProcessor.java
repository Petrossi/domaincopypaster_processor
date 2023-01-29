package com.domainsurvey.crawler.service.crawler.finalizer.impl.processor;

import com.domainsurvey.crawler.model.domain.Domain;
import com.domainsurvey.crawler.model.link.Node;
import com.domainsurvey.crawler.model.type.CrawlingStatus;
import com.domainsurvey.crawler.model.type.DownloadStatus;
import com.domainsurvey.crawler.model.type.FinalizerStatus;
import com.domainsurvey.crawler.model.type.NodeType;
import com.domainsurvey.crawler.service.crawler.finalizer.CrawlingFinalizerProcessor;
import com.domainsurvey.crawler.service.dao.QueryExecutor;
import com.domainsurvey.crawler.service.table.TableService;
import com.domainsurvey.crawler.service.table.type.SchemaType;
import com.domainsurvey.crawler.service.table.type.TableType;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class ResourceDownloaderProcessor implements CrawlingFinalizerProcessor {
    @Autowired
    protected QueryExecutor queryExecutor;

    public void process(Domain domain) {
        log.info("start: " + domain.getId());

        String nodeTable = TableService.getFullTableName(domain.getId(), SchemaType.PROCESS, TableType.NODE);

        String sql = String.format("SELECT id, url, typeFROM %s p where download_status = %s limit 1", nodeTable, DownloadStatus.PENDING);

        Node node = queryExecutor.queryForObject(sql, (rs, var1) -> {
            return Node.builder()
                    .id(rs.getLong("id"))
                    .url(rs.getString("url"))
                    .type(NodeType.fromValue(rs.getByte("type"))).build()
            ;
        });

        log.info("finished : " + domain.getId());
    }

    @Override
    public FinalizerStatus getNextStatus() {
        return FinalizerStatus.COUNT_PAGE_RANK;
    }
}