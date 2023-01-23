package com.domainsurvey.crawler.service.crawler.store;

import java.util.List;

import com.domainsurvey.crawler.model.link.Node;
import com.domainsurvey.crawler.service.crawler.model.PageResult;

public interface CrawlingStoreProcessor {
    void initQueue();

    void addNodesToQueue(List<Node> nodes);

    void processCrawlerResult(PageResult pageResult);
}