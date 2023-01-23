package com.domainsurvey.crawler.service.urlProcessor;

import com.domainsurvey.crawler.exception.NeedsRetryUrlProcessException;
import com.domainsurvey.crawler.service.crawler.model.PageResult;
import com.domainsurvey.crawler.service.urlProcessor.model.UrlProcessorConfig;

public interface UrlProcessor {
    PageResult processUrl(UrlProcessorConfig config) throws NeedsRetryUrlProcessException;
}