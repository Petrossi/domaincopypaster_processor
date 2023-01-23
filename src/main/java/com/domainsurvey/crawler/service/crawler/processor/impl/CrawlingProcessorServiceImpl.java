package com.domainsurvey.crawler.service.crawler.processor.impl;

import com.domainsurvey.crawler.model.domain.Domain;
import com.domainsurvey.crawler.service.crawler.CrawlerUtilsService;
import com.domainsurvey.crawler.service.crawler.importer.impl.ResultImporterProcessor;
import com.domainsurvey.crawler.service.crawler.importer.model.ImportStore;
import com.domainsurvey.crawler.service.crawler.processor.CrawlingProcessorService;
import com.domainsurvey.crawler.service.crawler.store.CrawlingStoreProcessor;
import com.domainsurvey.crawler.service.crawler.store.StateStoreProcessor;
import com.domainsurvey.crawler.service.crawler.store.model.CrawlingStore;
import com.domainsurvey.crawler.service.crawler.store.model.StateStore;
import com.domainsurvey.crawler.service.crawler.worker.PageWorkerManagerService;
import com.domainsurvey.crawler.service.dao.DomainCrawlingInfoService;
import com.domainsurvey.crawler.service.dao.page.FinalPageService;
import com.domainsurvey.crawler.service.dao.page.LastPageService;
import com.domainsurvey.crawler.service.dao.page.ProgressPageService;
import com.domainsurvey.crawler.service.robots.RobotsService;
import com.domainsurvey.crawler.service.robots.RobotsTxtParserService;
import com.domainsurvey.crawler.service.urlProcessor.HttpRequestInfoCache;
import com.domainsurvey.crawler.service.urlProcessor.impl.HttpRequestInfoCacheIml;
import com.domainsurvey.crawler.thread.CrawlingProcessorServiceThread;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("prototype")
@NoArgsConstructor
@Log4j2
public class CrawlingProcessorServiceImpl implements CrawlingProcessorService {

    private volatile boolean inited;

    private Domain domain;

    private PageWorkerManagerService urlWorkerService;

    public CrawlingStoreProcessor crawlingStoreProcessor;
    public StateStoreProcessor stateStoreProcessor;

    public ResultImporterProcessor resultImporterProcessor;

    public RobotsTxtParserService robotsTxtParserService;

    private final ImportStore importStore = new ImportStore();
    private final StateStore stateStore = new StateStore();
    private final CrawlingStore crawlingStore = new CrawlingStore();
    private final HttpRequestInfoCache httpRequestInfoCache = new HttpRequestInfoCacheIml();

    @Autowired
    public FinalPageService finalPageService;
    @Autowired
    public LastPageService lastPageService;
    @Autowired
    public DomainCrawlingInfoService domainCrawlingInfoService;
    @Autowired
    public CrawlerUtilsService crawlerUtilsService;
    @Autowired
    public ProgressPageService progressPageService;
    @Autowired
    public RobotsService robotsService;
    @Autowired
    protected BeanFactory beanFactory;

    public CrawlingProcessorServiceImpl(Domain domain) {
        this.domain = domain;
    }

    private volatile boolean isWorking = false;

    public void start() {
        if (inited) {
            log.info("already inited: {}", domain.getId());

            return;
        }

        log.info("start: {}", domain.getId());

        crawlingStoreProcessor = beanFactory.getBean(CrawlingStoreProcessor.class, this, stateStore);
        stateStoreProcessor = beanFactory.getBean(StateStoreProcessor.class, this, stateStore);

        stateStoreProcessor.init();

        try {
            robotsTxtParserService = robotsService.createRobotsTxtParserServiceByDomain(domain);
        } catch (Exception e) {
            e.printStackTrace();
        }

        resultImporterProcessor = beanFactory.getBean(ResultImporterProcessor.class, this);
        urlWorkerService = beanFactory.getBean(PageWorkerManagerService.class, this);

        initObserver();

        inited = true;

        checkIfNeedFinish();
    }

    @Override
    public Domain domain() {
        return domain;
    }

    @Override
    public ImportStore importStore() {
        return importStore;
    }

    @Override
    public StateStore stateStore() {
        return stateStore;
    }

    @Override
    public CrawlingStore crawlingStore() {
        return crawlingStore;
    }

    @Override
    public RobotsTxtParserService robotsTxtParserService() {
        return robotsTxtParserService;
    }

    @Override
    public StateStoreProcessor stateStoreProcessor() {
        return stateStoreProcessor;
    }

    public CrawlingStoreProcessor crawlingStoreProcessor() {
        return crawlingStoreProcessor;
    }

    public HttpRequestInfoCache httpRequestInfoCache() {
        return httpRequestInfoCache;
    }

    private void startProcessing() {
        isWorking = true;

        stateStoreProcessor.start();
        urlWorkerService.start();
        resultImporterProcessor.start();
    }

    public void stop(boolean finalizeImport) {
        log.info("try stop: {}", domain.getId());

        isWorking = false;

        if (urlWorkerService != null) {
            urlWorkerService.stop();
        }

        if (stateStoreProcessor != null) {
            stateStoreProcessor.stop();
        }

        if (finalizeImport && resultImporterProcessor != null) {
            resultImporterProcessor.stop();
        }

        log.info("stopped: {}", domain.getId());
    }

    private void initObserver() {
        if (!isWorking) {
            new CrawlingProcessorServiceThread(this).start();
        }
    }

    private boolean addNotFoundPages() {
        try {
//            long countOfLostPagesToView = lastPageService.countTotal(domain);
//
//            if (countOfLostPagesToView > 0) {
//                finalPageService.insertFromLastTable(domain);
//
//                return true;
//            }
        } catch (Exception ignored) {
        }

        return false;
    }

    @Override
    public void run() {
        log.info("initObserver: " + domain.getId());

        startProcessing();

        isWorking = true;
    }

    private void tryFinish() {
        crawlingStore().clearQueue();
        importStore().clear();

        if (addNotFoundPages()) {
            startProcessing();

            return;
        }

        isWorking = false;
        stop(true);
        crawlerUtilsService.finishDomainParsing(domain);
    }

    public void checkIfNeedFinish() {
        synchronized (stateStore) {
            if (stateStore.finishedImporting == 0 && stateStore.queue == 0) {
                tryFinish();
            }
        }
    }
}