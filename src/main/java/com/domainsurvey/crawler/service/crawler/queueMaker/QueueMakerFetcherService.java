package com.domainsurvey.crawler.service.crawler.queueMaker;

import java.util.List;

import com.domainsurvey.crawler.model.domain.Domain;
import com.domainsurvey.crawler.model.page.Page;

public interface QueueMakerFetcherService {
    List<Page> getPagesByLimit(Domain domain, long limit);
}