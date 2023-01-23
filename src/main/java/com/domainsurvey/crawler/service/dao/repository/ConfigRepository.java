package com.domainsurvey.crawler.service.dao.repository;

import com.domainsurvey.crawler.model.domain.Config;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfigRepository extends CrudRepository<Config, Long> {

}
