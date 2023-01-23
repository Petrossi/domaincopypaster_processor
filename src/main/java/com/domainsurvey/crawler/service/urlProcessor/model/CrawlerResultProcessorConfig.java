package com.domainsurvey.crawler.service.urlProcessor.model;

import lombok.AllArgsConstructor;

import com.domainsurvey.crawler.model.domain.Domain;
import com.domainsurvey.crawler.model.link.Node;
import com.domainsurvey.crawler.service.fetcher.model.HttpResult;
import com.domainsurvey.crawler.service.robots.RobotsTxtParserService;
import com.domainsurvey.crawler.service.urlProcessor.HttpRequestInfoCache;

@AllArgsConstructor
public class CrawlerResultProcessorConfig {
    public HttpResult httpResult;
    public Node nodeToProcess;
    public Domain domain;
    public RobotsTxtParserService robotsTxtParserService;
    public HttpRequestInfoCache httpRequestInfoCache;
}
