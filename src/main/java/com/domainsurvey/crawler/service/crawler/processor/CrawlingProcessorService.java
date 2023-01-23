package com.domainsurvey.crawler.service.crawler.processor;

import com.domainsurvey.crawler.model.domain.Domain;
import com.domainsurvey.crawler.service.crawler.importer.model.ImportStore;
import com.domainsurvey.crawler.service.crawler.store.CrawlingStoreProcessor;
import com.domainsurvey.crawler.service.crawler.store.StateStoreProcessor;
import com.domainsurvey.crawler.service.crawler.store.model.CrawlingStore;
import com.domainsurvey.crawler.service.crawler.store.model.StateStore;
import com.domainsurvey.crawler.service.robots.RobotsTxtParserService;
import com.domainsurvey.crawler.service.urlProcessor.HttpRequestInfoCache;

public interface CrawlingProcessorService extends Runnable {
    void stop(boolean finalizeImport);

    void start();

    void checkIfNeedFinish();

    Domain domain();

    ImportStore importStore();

    StateStore stateStore();

    CrawlingStore crawlingStore();

    RobotsTxtParserService robotsTxtParserService();

    StateStoreProcessor stateStoreProcessor();

    CrawlingStoreProcessor crawlingStoreProcessor();

    HttpRequestInfoCache httpRequestInfoCache();
}