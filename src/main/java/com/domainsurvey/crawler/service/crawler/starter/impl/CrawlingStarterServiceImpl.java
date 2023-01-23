package com.domainsurvey.crawler.service.crawler.starter.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.stereotype.Service;
import com.domainsurvey.crawler.exception.CantCreateCrawlingTablesException;
import com.domainsurvey.crawler.exception.CrawlingPageTableEmptyException;
import com.domainsurvey.crawler.model.domain.Domain;
import com.domainsurvey.crawler.model.domain.DomainCrawlingInfo;
import com.domainsurvey.crawler.model.type.CrawlingStatus;
import com.domainsurvey.crawler.service.backend.BackendService;
import com.domainsurvey.crawler.service.crawler.CrawlerUtilsService;
import com.domainsurvey.crawler.service.crawler.DomainStorage;
import com.domainsurvey.crawler.service.crawler.finalizer.CrawlingFinalizerService;
import com.domainsurvey.crawler.service.crawler.processor.CrawlingProcessorService;
import com.domainsurvey.crawler.service.crawler.starter.CrawlingStarterService;
import com.domainsurvey.crawler.service.dao.DomainCrawlingInfoService;
import com.domainsurvey.crawler.service.dao.DomainService;
import com.domainsurvey.crawler.service.fetcher.FetcherProcessor;
import com.domainsurvey.crawler.service.table.TableService;
import com.domainsurvey.crawler.thread.CrawlingStarterProcessStartingThread;
import com.domainsurvey.crawler.web.ws.publisher.ProgressPublisherService;

@Service
@RequiredArgsConstructor
@Log4j2
public class CrawlingStarterServiceImpl implements CrawlingStarterService {

    private final DomainService domainService;
    private final FetcherProcessor fetcherProcessor;
    private final DomainCrawlingInfoService domainCrawlingInfoService;
    private final TableService tableService;
    private final CrawlerUtilsService crawlerUtilsService;
    private final BeanFactory beanFactory;
    private final BackendService backendService;
    private final DomainStorage domainStorage;
    private final ProgressPublisherService progressPublisherService;

    public void processDomain(Domain domain) {
        log.info("processDomain: " + domain.getId());

        domain.setStatus(CrawlingStatus.CRAWLING);

        DomainCrawlingInfo domainInfo = domain.getProcessDomainCrawlingInfo();

        domainService.save(domain);
        domainCrawlingInfoService.save(domainInfo);
        backendService.updateDomainStatus(domain.getId(), CrawlingStatus.CRAWLING);

        new CrawlingStarterProcessStartingThread(() -> {
            boolean spa = fetcherProcessor.processIsSpa(domain);

            domain.getConfig().setSpa(spa);

            domainService.save(domain);

            boolean processed = false;
            try {
                Domain lastCrawledDomain = domainService.getRecentlyCrawledDomain(domain);

                if (lastCrawledDomain != null && domain.getCrawlCount() == 0) {
                    copyLastCrawledDomainTablesToCurrentDomain(domain, lastCrawledDomain);

                    processed = true;
                }
            } catch (Exception e) {
                tableService.deleteAllCrawlingTables(domain.getId());
                e.printStackTrace();
            }
            if (!processed) {
                try {
                    startProcessDomain(domain);
                } catch (CantCreateCrawlingTablesException e) {
                    domain.setStatus(CrawlingStatus.STARTING_FAILED);

                    domainService.save(domain);
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void startProcessDomain(Domain domain) throws CantCreateCrawlingTablesException {
        String domainUrl = domain.getUrl();

        log.info("Adding to domainsInProcess: " + domainUrl + " " + domain.getId());

        crawlerUtilsService.createCrawlingTables(domain);

        progressPublisherService.publishStarted(domain.getId());

        CrawlingProcessorService crawlingProcessorService = beanFactory.getBean(CrawlingProcessorService.class, domain);

        domainStorage.getDomainsInProcess().put(domain.getId(), crawlingProcessorService);

        crawlingProcessorService.start();
    }

    public void copyLastCrawledDomainTablesToCurrentDomain(Domain domain, Domain lastCrawledDomain) {
        log.info("copyLastCrawled from: " + lastCrawledDomain.getId() + " to: " + domain.getId());

        tableService.copyDomainsFinalPageTables(lastCrawledDomain, domain);

        try {
            beanFactory.getBean(CrawlingFinalizerService.class).finalizeDomainParsing(domain);
        } catch (CrawlingPageTableEmptyException e) {
            beanFactory.getBean(CrawlingFinalizerService.class).hardRestartDomain(domain);
        }
    }
}