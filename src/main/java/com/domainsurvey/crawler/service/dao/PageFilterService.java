package com.domainsurvey.crawler.service.dao;

import com.domainsurvey.crawler.model.filter.DomainFilter;

public interface PageFilterService {
    void save(DomainFilter domain);
}