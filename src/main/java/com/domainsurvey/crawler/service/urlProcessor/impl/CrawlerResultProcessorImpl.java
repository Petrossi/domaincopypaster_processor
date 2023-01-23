package com.domainsurvey.crawler.service.urlProcessor.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.springframework.stereotype.Service;
import com.domainsurvey.crawler.exception.MaxRedirectCountException;
import com.domainsurvey.crawler.service.crawler.model.PageResult;
import com.domainsurvey.crawler.service.urlProcessor.CrawlerResultProcessor;
import com.domainsurvey.crawler.service.urlProcessor.model.CrawlerResultProcessorConfig;

@Service
@RequiredArgsConstructor
@Log4j2
public class CrawlerResultProcessorImpl implements CrawlerResultProcessor {

    public PageResult processHttpResult(CrawlerResultProcessorConfig config) throws MaxRedirectCountException {
        PageResult pageResult = PageResult.builder()
                .httpResult(config.httpResult)
                .linkToProcess(config.nodeToProcess)
                .domain(config.domain)
                .robotsTxtParserService(config.robotsTxtParserService)
                .build();

        log.debug(
                "new urls from: {} | int: {} ext: {} id: {}",
                config.nodeToProcess.getUrl(), pageResult.getNodes().size(), pageResult.getEdges().size(), config.domain.getId()
        );

        return pageResult;
    }
}