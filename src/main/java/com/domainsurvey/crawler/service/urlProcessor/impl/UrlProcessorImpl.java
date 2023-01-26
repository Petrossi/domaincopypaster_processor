package com.domainsurvey.crawler.service.urlProcessor.impl;

import com.domainsurvey.crawler.model.domain.Domain;
import com.domainsurvey.crawler.service.PageCacheManager;
import com.domainsurvey.crawler.utils.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import static com.domainsurvey.crawler.service.fetcher.model.HttpConfig.DESCRIPTOR_METHOD;
import static com.domainsurvey.crawler.service.fetcher.model.HttpConfig.HTTP_METHOD;
import static com.domainsurvey.crawler.utils.Constants.BAD_STATUS_CODES;
import static com.domainsurvey.crawler.utils.Utils.convertFomDBValidValueURL;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import com.domainsurvey.crawler.exception.NeedsRetryUrlProcessException;
import com.domainsurvey.crawler.model.link.Edge;
import com.domainsurvey.crawler.model.link.Node;
import com.domainsurvey.crawler.model.type.NodeType;
import com.domainsurvey.crawler.service.crawler.model.PageResult;
import com.domainsurvey.crawler.service.fetcher.FetcherProcessor;
import com.domainsurvey.crawler.service.fetcher.model.HttpConfig;
import com.domainsurvey.crawler.service.fetcher.model.HttpResult;
import com.domainsurvey.crawler.service.urlProcessor.CrawlerResultProcessor;
import com.domainsurvey.crawler.service.urlProcessor.UrlProcessor;
import com.domainsurvey.crawler.service.urlProcessor.model.CrawlerResultProcessorConfig;
import com.domainsurvey.crawler.service.urlProcessor.model.UrlProcessorConfig;

@Service
@RequiredArgsConstructor
@Log4j2
public class UrlProcessorImpl implements UrlProcessor {

    private final FetcherProcessor fetcherProcessor;
    private final CrawlerResultProcessor crawlerResultProcessor;

    @Autowired
    private PageCacheManager pageCacheManager;

    public PageResult processUrl(UrlProcessorConfig config) throws NeedsRetryUrlProcessException {
        String validUrlToProcess = config.nodeToProcess.getUrl();
        PageResult pageResult = new PageResult();
        pageResult.setId(config.nodeToProcess.getId());
        try {
            validUrlToProcess = convertFomDBValidValueURL(config.nodeToProcess.getUrl());
        } catch (IllegalArgumentException e) {
            log.warn("{} -> {}", e.getMessage(), validUrlToProcess);
        }

        short statusCode = (short) HttpStatus.BAD_GATEWAY.value();
        pageResult.setStatusCode(statusCode);

        Set<Node> nodes = new HashSet<>();
        List<Edge> edges = new ArrayList<>();
        HttpResult httpResult = null;
        try {
            HttpConfig.HttpConfigBuilder httpConfigBuilder = HttpConfig.builder().url(validUrlToProcess).proxy(config.needsProxy).clearHtml(false).serviceType(config.domain.getConfig().isSpa() ? DESCRIPTOR_METHOD : HTTP_METHOD);

            if (config.nodeToProcess.getType() != NodeType.INTERNAL) {
                httpConfigBuilder.onlyHeaders(true);
            }

            HttpConfig httpConfig = httpConfigBuilder.build();

            httpResult = fetcherProcessor.getPage(httpConfig);

            CrawlerResultProcessorConfig crawlerResultProcessorConfig = new CrawlerResultProcessorConfig(httpResult, config.nodeToProcess, config.domain, config.robotsTxtParserService, config.httpRequestInfoCache);

            pageResult = crawlerResultProcessor.processHttpResult(crawlerResultProcessorConfig);

            finishProcessMessage(config.domain, httpResult, config.nodeToProcess.getId());
        } catch (Exception e) {
            e.printStackTrace();
            pageResult.setNodes(nodes);
            pageResult.setEdges(edges);
        }

        if (BAD_STATUS_CODES.contains(pageResult.getStatusCode()) || (pageResult.getStatusCode() == HttpStatus.BAD_GATEWAY.value() && !config.needsProxy)) {
            if (httpResult != null && httpResult.proxy != null) {
                log.debug("{} : {} -> proxy {}", validUrlToProcess, pageResult.getStatusCode(), httpResult.proxy);
            }
            throw new NeedsRetryUrlProcessException(false, pageResult.getStatusCode());
        }
        if ((pageResult.getStatusCode() == HttpStatus.NOT_FOUND.value())) {
            throw new NeedsRetryUrlProcessException(true, pageResult.getStatusCode());
        }


        return pageResult;
    }

    private void finishProcessMessage(Domain domain, HttpResult httpResult, long id) {
        if (httpResult.httpStatusCode == 200) {
            pageCacheManager.savePage(Utils.getCRC32(domain.getUrl()), httpResult, id);
        }
    }
}