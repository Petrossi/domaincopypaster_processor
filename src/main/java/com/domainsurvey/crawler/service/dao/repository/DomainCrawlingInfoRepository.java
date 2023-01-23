package com.domainsurvey.crawler.service.dao.repository;

import com.domainsurvey.crawler.model.domain.DomainCrawlingInfo;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DomainCrawlingInfoRepository extends CrudRepository<DomainCrawlingInfo, Long> {

}
