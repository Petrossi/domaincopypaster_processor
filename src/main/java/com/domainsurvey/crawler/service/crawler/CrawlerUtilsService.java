package com.domainsurvey.crawler.service.crawler;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import static com.domainsurvey.crawler.utils.Utils.getCurrentTimestamp;

import org.springframework.stereotype.Service;
import com.domainsurvey.crawler.exception.CantCreateCrawlingTablesException;
import com.domainsurvey.crawler.model.domain.Domain;
import com.domainsurvey.crawler.model.domain.DomainCrawlingInfo;
import com.domainsurvey.crawler.model.type.CrawlingPriority;
import com.domainsurvey.crawler.model.type.CrawlingStatus;
import com.domainsurvey.crawler.service.crawler.finalizer.CrawlingFinalizerService;
import com.domainsurvey.crawler.service.dao.DomainCrawlingInfoService;
import com.domainsurvey.crawler.service.dao.QueryExecutor;
import com.domainsurvey.crawler.service.table.TableService;

@Service
@RequiredArgsConstructor
@Log4j2
public class CrawlerUtilsService {

    private final TableService tableService;
    private final QueryExecutor queryExecutor;
    private final CrawlingFinalizerService crawlingFinalizerService;
    private final DomainCrawlingInfoService domainCrawlingInfoService;
    private final DomainStorage domainStorage;

    public void startLastSession() {
        log.info("startLastSession");
        String sql2 = String.format(
                "UPDATE domain SET status = %s, priority = %s WHERE status = %s;",
                CrawlingStatus.QUEUE, CrawlingPriority.RESTART, CrawlingStatus.CRAWLING
        );

        log.info(sql2);

        queryExecutor.executeQuery(sql2);
    }

    public void finishDomainParsing(Domain domain) {
        log.info("finishDomainParsing : " + domain.getId());

        DomainCrawlingInfo domainCrawlingInfo = domain.getProcessDomainCrawlingInfo();

        try {
            domainCrawlingInfo.setCrawlingFinishedTimestamp(getCurrentTimestamp());
            domainCrawlingInfoService.save(domainCrawlingInfo);
        } catch (Exception ignored) {
        }

        log.info("=================== {} : Finished==========================", domain.getUrl());

        crawlingFinalizerService.addToQueue(domain);

        removeFromDomainsInProcessById(domain.getId());
    }

    private void removeFromDomainsInProcessById(String id) {
        domainStorage.getDomainsInProcess().remove(id);
    }

    public void createCrawlingTables(Domain domain) throws CantCreateCrawlingTablesException {
        tableService.createCrawlerProcessTables(domain);
    }
}