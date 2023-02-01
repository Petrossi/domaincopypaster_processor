package com.domainsurvey.crawler.service.dao;

import com.domainsurvey.crawler.model.domain.Domain;
import com.domainsurvey.crawler.model.type.CrawlingStatus;

import java.util.List;
import java.util.Optional;

public interface DomainService {
    void save(Domain domain);

    void deleteById(String id);

    Optional<Domain> find(String id);

    Optional<Domain> findByHost(String host);

    Optional<Domain> findDeleted();

    Domain getRecentlyCrawledDomain(Domain domain);

    List<Domain> findByCrawlingStatus(CrawlingStatus crawlingStatus);

    void addToCrawling(Domain domain);

    Optional<Domain> findFirstByCrawlingStatus(CrawlingStatus crawlingStatus);

    Optional<Domain> getOneNewDomainForCrawling();
}