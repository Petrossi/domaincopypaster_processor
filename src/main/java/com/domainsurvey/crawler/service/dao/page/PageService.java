package com.domainsurvey.crawler.service.dao.page;

import com.domainsurvey.crawler.model.domain.Domain;
import com.domainsurvey.crawler.model.type.FilterImportance;

public interface PageService {
    long countTotal(Domain domain);

    long countInternalTotal(Domain domain);

    long countBadStatusCode(Domain domain);

    long countByFilterImportance(Domain domain, FilterImportance filterImportance);

    long countNoIssue(Domain domain);

    double countTotalScore(Domain domain);
}
