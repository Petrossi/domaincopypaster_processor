package com.domainsurvey.crawler.service.dao;

import java.util.List;

import com.domainsurvey.crawler.model.domain.DomainCrawlingInfo;
import com.domainsurvey.crawler.model.filter.DomainFilter;

public interface DomainFilterService {
    List<DomainFilter> findByDomainCrawlingInfo(DomainCrawlingInfo domainCrawlingInfo);

    List<Integer> findForFillDomainCrawlingId(DomainCrawlingInfo domainCrawlingInfoId);

    void saveAll(List<DomainFilter> domainFilters);

    void save(DomainFilter domainFilter);

}