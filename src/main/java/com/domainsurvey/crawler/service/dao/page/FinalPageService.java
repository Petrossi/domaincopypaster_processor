package com.domainsurvey.crawler.service.dao.page;

import com.domainsurvey.crawler.model.domain.Domain;

public interface FinalPageService extends PageService {
    void insertFromLastTable(Domain domain);
}