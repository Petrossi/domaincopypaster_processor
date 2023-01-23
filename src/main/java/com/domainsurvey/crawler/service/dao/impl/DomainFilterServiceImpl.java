package com.domainsurvey.crawler.service.dao.impl;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.stereotype.Service;
import com.domainsurvey.crawler.model.domain.DomainCrawlingInfo;
import com.domainsurvey.crawler.model.filter.DomainFilter;
import com.domainsurvey.crawler.service.dao.DomainFilterService;
import com.domainsurvey.crawler.service.dao.QueryExecutor;
import com.domainsurvey.crawler.service.dao.repository.DomainFilterRepository;

@Service
@RequiredArgsConstructor
public class DomainFilterServiceImpl implements DomainFilterService {

    private final DomainFilterRepository domainFilterRepository;
    private final QueryExecutor queryExecutor;

    @Override
    public List<DomainFilter> findByDomainCrawlingInfo(DomainCrawlingInfo domainCrawlingInfo) {
        return domainFilterRepository.findAllByDomainCrawlingInfoId(domainCrawlingInfo.getId());
    }

    @Override
    public List<Integer> findForFillDomainCrawlingId(DomainCrawlingInfo domainCrawlingInfo) {
        String template = "select df.id from domain_filter df where df.domain_crawling_info_id = %s";

        String sql = String.format(template, domainCrawlingInfo.getId());

        return queryExecutor.queryForList(sql, Integer.class);
    }

    @Override
    public void saveAll(List<DomainFilter> domainFilters) {
        domainFilterRepository.saveAll(domainFilters);
    }

    @Override
    public void save(DomainFilter domainFilter) {
        domainFilterRepository.save(domainFilter);
    }
}