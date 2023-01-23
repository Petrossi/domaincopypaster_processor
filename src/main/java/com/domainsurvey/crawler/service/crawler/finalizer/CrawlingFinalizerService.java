package com.domainsurvey.crawler.service.crawler.finalizer;

import com.domainsurvey.crawler.exception.CrawlingPageTableEmptyException;
import com.domainsurvey.crawler.model.domain.Domain;

public interface CrawlingFinalizerService {
    void hardRestartDomain(Domain domain);

    void finalizeDomainParsing(Domain domain) throws CrawlingPageTableEmptyException;

    void saveDateFinalizerStarted(Domain domain);

    void addToQueue(Domain domain);

    void start();
}