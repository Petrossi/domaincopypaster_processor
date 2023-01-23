package com.domainsurvey.crawler.service.crawler.starter;

import com.domainsurvey.crawler.model.domain.Domain;

public interface CrawlingStarterService {
    void processDomain(Domain domain);
}