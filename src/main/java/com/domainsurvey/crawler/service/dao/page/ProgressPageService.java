package com.domainsurvey.crawler.service.dao.page;

import com.domainsurvey.crawler.model.domain.Domain;
import com.domainsurvey.crawler.model.page.Page;

public interface ProgressPageService extends PageService {

    void create(Page page, Domain domain);
}