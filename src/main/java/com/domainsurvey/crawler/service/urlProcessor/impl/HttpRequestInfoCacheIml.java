package com.domainsurvey.crawler.service.urlProcessor.impl;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.domainsurvey.crawler.service.fetcher.model.HttpRequestInfo;
import com.domainsurvey.crawler.service.urlProcessor.HttpRequestInfoCache;

public class HttpRequestInfoCacheIml implements HttpRequestInfoCache {
    private Map<Long, HttpRequestInfo> cache = new ConcurrentHashMap<>(10000);

    public void addToCache(Long id, HttpRequestInfo httpRequestInfo) {
        cache.put(id, httpRequestInfo);
    }

    public Optional<HttpRequestInfo> getFromCache(Long id) {
        return Optional.ofNullable(cache.get(id));
    }
}