package com.domainsurvey.crawler.service.urlProcessor;

import java.util.Optional;

import com.domainsurvey.crawler.service.fetcher.model.HttpRequestInfo;

public interface HttpRequestInfoCache {
    void addToCache(Long id, HttpRequestInfo httpRequestInfo);

    Optional<HttpRequestInfo> getFromCache(Long id);
}
