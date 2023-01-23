package com.domainsurvey.crawler.service.crawler.finalizer.impl.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.springframework.stereotype.Service;
import com.domainsurvey.crawler.model.domain.Domain;
import com.domainsurvey.crawler.model.type.FinalizerStatus;
import com.domainsurvey.crawler.service.crawler.finalizer.CrawlingFinalizerProcessor;
import com.domainsurvey.crawler.service.table.TableService;
import com.domainsurvey.crawler.service.table.type.SchemaType;

@Service
@RequiredArgsConstructor
@Log4j2
public class ImportIntoFinalTablesProgressProcessor implements CrawlingFinalizerProcessor {

    private final TableService tableService;

    @Override
    public FinalizerStatus getNextStatus() {
        return FinalizerStatus.FINALIZING;
    }

    public void process(Domain domain) {
        log.info("start: " + domain.getId());

        moveFinalToLast(domain);

        moveProcessToFinal(domain);

        log.info("finished : " + domain.getId());
    }

    public void moveProcessToFinal(Domain domain) {
        tableService.deleteFinalTables(domain);

        TableService.BASE_CRAWLER_TABLES.forEach(
                tableType -> tableService.moveFromSchemaToSchema(domain.getId(), SchemaType.PROCESS, SchemaType.FINAL, tableType)
        );
    }

    public void moveFinalToLast(Domain domain) {
        tableService.deleteLastCrawlingTables(domain.getId());

        TableService.BASE_CRAWLER_TABLES.forEach(
                tableType -> tableService.moveFromSchemaToSchema(domain.getId(), SchemaType.FINAL, SchemaType.LAST, tableType)
        );
    }
}