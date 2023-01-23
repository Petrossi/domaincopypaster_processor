package com.domainsurvey.crawler.service.dao.repository;

import java.util.List;
import java.util.Optional;

import com.domainsurvey.crawler.model.domain.Domain;
import com.domainsurvey.crawler.model.type.CrawlingStatus;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DomainRepository extends CrudRepository<Domain, String> {
    List<Domain> findAllByStatus(CrawlingStatus crawlingStatus);

    Optional<Domain> findFirstByStatus(CrawlingStatus crawlingStatus);
}
