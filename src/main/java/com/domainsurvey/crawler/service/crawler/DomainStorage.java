package com.domainsurvey.crawler.service.crawler;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;
import com.domainsurvey.crawler.exception.DomainQueueEmptyException;
import com.domainsurvey.crawler.model.domain.Domain;
import com.domainsurvey.crawler.model.domain.DomainCrawlingInfo;
import com.domainsurvey.crawler.model.type.CrawlingStatus;
import com.domainsurvey.crawler.service.crawler.processor.CrawlingProcessorService;
import com.domainsurvey.crawler.service.dao.DomainService;

@Component
@RequiredArgsConstructor
@Log4j2
public class DomainStorage {
    private final Object lock = new Object();
    private Map<String, CrawlingProcessorService> domainsInProcess = Collections.synchronizedMap(new HashMap<>());

    private final DomainService domainService;

    public void addToQueue(Domain domain) {
        if (!domain.getStatus().equals(CrawlingStatus.QUEUE)) {
            DomainCrawlingInfo domainCrawlingInfo = DomainCrawlingInfo.builder().domain(domain).build();

            domain.setStatus(CrawlingStatus.QUEUE);

            domain.setProcessDomainCrawlingInfo(domainCrawlingInfo);

            domainService.save(domain);

            log.info("Adding domain to queue: " + domain.getId());
        }
    }

    public synchronized Domain getNextDomain() throws DomainQueueEmptyException {
        Domain domain;

        synchronized (lock) {
            Optional<Domain> optional;
            try {
                optional = domainService.getOneNewDomainForCrawling();
            } catch (EmptyResultDataAccessException e) {
                throw new DomainQueueEmptyException();
            }

            if (!optional.isPresent()) {
                throw new DomainQueueEmptyException();
            }

            domain = optional.get();

            Optional<Domain> refreshedDomain = domainService.find(domain.getId());
            if (refreshedDomain.isPresent()) {
                domain = refreshedDomain.get();
            }
        }

        return domain;
    }

    public Map<String, CrawlingProcessorService> getDomainsInProcess() {
        return domainsInProcess;
    }
}