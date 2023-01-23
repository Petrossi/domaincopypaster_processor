package com.domainsurvey.crawler.service.urlProcessor;

import com.domainsurvey.crawler.exception.MaxRedirectCountException;
import com.domainsurvey.crawler.service.crawler.model.PageResult;
import com.domainsurvey.crawler.service.urlProcessor.model.CrawlerResultProcessorConfig;

public interface CrawlerResultProcessor {
    PageResult processHttpResult(CrawlerResultProcessorConfig config) throws MaxRedirectCountException;
}