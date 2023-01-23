package com.domainsurvey.crawler.service.dao;

import java.util.Optional;

import com.domainsurvey.crawler.model.domain.DomainCrawlingInfo;

public interface DomainCrawlingInfoService {
    Optional<DomainCrawlingInfo> find(Long id);

    void save(DomainCrawlingInfo domainInfo);
}