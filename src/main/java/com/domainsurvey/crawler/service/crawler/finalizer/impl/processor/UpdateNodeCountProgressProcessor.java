package com.domainsurvey.crawler.service.crawler.finalizer.impl.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.springframework.stereotype.Service;
import com.domainsurvey.crawler.model.domain.Domain;
import com.domainsurvey.crawler.model.type.FinalizerStatus;
import com.domainsurvey.crawler.service.crawler.finalizer.CrawlingFinalizerProcessor;
import com.domainsurvey.crawler.service.dao.QueryExecutor;
import com.domainsurvey.crawler.service.table.TableService;
import com.domainsurvey.crawler.service.table.type.SchemaType;
import com.domainsurvey.crawler.service.table.type.TableType;

@Service
@RequiredArgsConstructor
@Log4j2
public class UpdateNodeCountProgressProcessor implements CrawlingFinalizerProcessor {

    private final QueryExecutor queryExecutor;

    public void process(Domain domain) {
        log.info("start: " + domain.getId());

        String template =
                "UPDATE %s SET incoming_count_total = (" +
                        "       SELECT" +
                        "           count(e.target_id)" +
                        "       FROM %s e" +
                        "       WHERE e.target_id = %s.id" +
                        ");";

        String sql = String.format(
                template,
                TableService.getFullTableName(domain.getId(), SchemaType.PROCESS, TableType.PAGE),
                TableService.getFullTableName(domain.getId(), SchemaType.PROCESS, TableType.EDGE),
                TableService.getFullTableName(domain.getId(), SchemaType.PROCESS, TableType.PAGE)
        );

        queryExecutor.executeQuery(sql);

        log.info("finished : " + domain.getId());
    }

    @Override
    public FinalizerStatus getNextStatus() {
        return FinalizerStatus.UPDATE_REDIRECTED_LINKS;
    }
}