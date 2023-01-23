package com.domainsurvey.crawler.service.crawler.finalizer;

import com.domainsurvey.crawler.exception.CrawlingPageTableEmptyException;
import com.domainsurvey.crawler.model.domain.Domain;
import com.domainsurvey.crawler.model.type.FinalizerStatus;

public interface CrawlingFinalizerProcessor {
    void process(Domain domain) throws CrawlingPageTableEmptyException;

    FinalizerStatus getNextStatus();
}