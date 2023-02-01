package com.domainsurvey.crawler.service.dao.repository;

import com.domainsurvey.crawler.model.domain.Domain;
import com.domainsurvey.crawler.model.type.CrawlingStatus;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DomainRepository extends CrudRepository<Domain, String> {
    List<Domain> findAllByStatus(CrawlingStatus crawlingStatus);

    Optional<Domain> findFirstByStatus(CrawlingStatus crawlingStatus);

    Optional<Domain> findByHost(String host);
}
