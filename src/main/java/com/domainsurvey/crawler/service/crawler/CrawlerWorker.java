package com.domainsurvey.crawler.service.crawler;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import static com.domainsurvey.crawler.utils.Utils.sleepSeconds;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.domainsurvey.crawler.exception.DomainQueueEmptyException;
import com.domainsurvey.crawler.exception.MaxDomainsParsingLimitException;
import com.domainsurvey.crawler.model.domain.Domain;
import com.domainsurvey.crawler.model.type.CrawlingStatus;
import com.domainsurvey.crawler.service.crawler.starter.CrawlingStarterService;
import com.domainsurvey.crawler.service.dao.QueryExecutor;
import com.domainsurvey.crawler.thread.CrawlerWorkerThread;

@Service
@RequiredArgsConstructor
@Log4j2
public class CrawlerWorker implements Runnable {

    @Value("${crawler.processor.maxParsingDomainsPerTime}")
    private int maxParsingDomainsPerTime;

    private final DomainStorage domainStorage;
    private final CrawlingStarterService crawlingStarterService;
    private final QueryExecutor queryExecutor;

    private Thread worker;

    @Override
    public void run() {
        log.info("start");
        while (true) {
            try {
                checkAndParseNextDomains();

            } catch (Exception e) {
                log.error(e.toString());
            }

            sleepSeconds(1);
        }
    }

    public void startTaskMonitor() {
        log.info("startTaskMonitor");

        if (worker == null) {
            worker = new CrawlerWorkerThread(this);
            worker.start();
        }
    }

    private void checkAndParseNextDomains() {
        while (true) {
            try {
                checkAndParseNextDomain();
            } catch (MaxDomainsParsingLimitException | DomainQueueEmptyException e) {
                break;
            }
        }
    }

    public void checkAndParseNextDomain() throws MaxDomainsParsingLimitException, DomainQueueEmptyException {
        Domain nextDomainToParse;

        nextDomainToParse = domainStorage.getNextDomain();

        validateMaxDomainsInProcess(false);

        try {
            crawlingStarterService.processDomain(nextDomainToParse);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void validateMaxDomainsInProcess(boolean isMonitoring) throws MaxDomainsParsingLimitException {
        String sql = "SELECT COUNT(*) from domain where status = " + CrawlingStatus.CRAWLING;

        int domainsCount = queryExecutor.queryForInteger(sql);

        int currentParsingDomainsPerTime = maxParsingDomainsPerTime;
        if (!isMonitoring) {
            currentParsingDomainsPerTime = currentParsingDomainsPerTime + 30;
        }
        if (isMonitoring && domainsCount >= currentParsingDomainsPerTime) {
            throw new MaxDomainsParsingLimitException();
        }
    }
}