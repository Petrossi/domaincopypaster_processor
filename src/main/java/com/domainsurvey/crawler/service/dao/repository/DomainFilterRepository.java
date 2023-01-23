package com.domainsurvey.crawler.service.dao.repository;

import java.util.List;

import com.domainsurvey.crawler.model.filter.DomainFilter;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DomainFilterRepository extends CrudRepository<DomainFilter, Long> {
    List<DomainFilter> findAllByDomainCrawlingInfoId(Long domainCrawlingInfo);
}
