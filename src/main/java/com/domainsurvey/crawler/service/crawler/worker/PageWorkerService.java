package com.domainsurvey.crawler.service.crawler.worker;

import com.domainsurvey.crawler.exception.NeedsRetryUrlProcessException;
import com.domainsurvey.crawler.model.domain.Domain;
import com.domainsurvey.crawler.model.link.Node;
import com.domainsurvey.crawler.service.crawler.model.PageResult;
import com.domainsurvey.crawler.service.crawler.processor.CrawlingProcessorService;
import com.domainsurvey.crawler.service.urlProcessor.UrlProcessor;
import com.domainsurvey.crawler.service.urlProcessor.model.PageMetaData;
import com.domainsurvey.crawler.service.urlProcessor.model.UrlProcessorConfig;
import com.domainsurvey.crawler.thread.PageWorkerThread;
import com.domainsurvey.crawler.utils.robots.SimpleRobotRules;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import static com.domainsurvey.crawler.utils.Utils.sleepSeconds;

@Service
@Scope("prototype")
@Log4j2
public class PageWorkerService implements Runnable {

    @Autowired
    private UrlProcessor urlProcessor;

    @Value("#{new Boolean('${crawler.proxy.enabled}')}")
    private boolean proxyEnabled;

    private volatile boolean isWorking = true;
    private volatile boolean isPaused;
    private volatile boolean inited;

    private Domain domain;

    private CrawlingProcessorService crawlingProcessorService;

    public PageWorkerService(CrawlingProcessorService crawlingProcessorService) {
        this.domain = crawlingProcessorService.domain();
        this.crawlingProcessorService = crawlingProcessorService;
    }

    public PageWorkerService() {
    }

    public void stop() {
        isWorking = false;
    }

    public void start() {
        isPaused = false;

        if (!inited) {
            log.info("start: {}", domain.getId());

            new PageWorkerThread(this).start();

            inited = true;
        }
    }

    private boolean useProxy = true;

    private PageResult processMessage(Node nodeToProcess) {
        log.debug("{} try process: {}", domain.getId(), nodeToProcess.getUrl());
        long processingStart = System.currentTimeMillis();

        int maxRetryCount = 5;
        int retryCount = 0;

        PageResult pageResult = null;

        boolean robotsValid = true;
        String robotsRule = "";
        if (crawlingProcessorService.robotsTxtParserService() != null) {
            SimpleRobotRules.RobotRule rule = crawlingProcessorService.robotsTxtParserService().isAllowed(nodeToProcess.getUrl());
            if (rule != null) {
                robotsValid = rule._allow;
                robotsRule = rule._prefix;
            }
        }

        short lastStatusCode = 999;

        while (maxRetryCount > retryCount) {
            boolean needsProxy = retryCount > 0 || (proxyEnabled && !useProxy);
            retryCount++;

            UrlProcessorConfig config = new UrlProcessorConfig(
                    nodeToProcess,
                    domain,
                    needsProxy,
                    crawlingProcessorService.robotsTxtParserService(),
                    crawlingProcessorService.httpRequestInfoCache()
            );

            try {
                pageResult = urlProcessor.processUrl(config);
                break;
            } catch (NeedsRetryUrlProcessException e) {
                lastStatusCode = e.getStatusCode();

                log.error("failed to process page: {} with {}", nodeToProcess.getUrl(), e.getStatusCode());

            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }

        if (pageResult == null) {
            pageResult = new PageResult();
            pageResult.setId(nodeToProcess.getId());
            pageResult.setNodeType(nodeToProcess.getType());
            pageResult.setRobotsValid(robotsValid);
            pageResult.setNodeType(nodeToProcess.getType());
            pageResult.markByPageMetaData(
                    PageMetaData.builder1()
                            .statusCode(lastStatusCode)
                            .robotsValid(robotsValid)
                            .robotsRule(robotsRule)
                            .url(nodeToProcess.getUrl())
                            .build()
            );
        }

        log.info(
                "{} | time:{} | int:{} | ext:{} | code:{}: time: {} | rTime: {}",
                (System.currentTimeMillis() - processingStart),
                nodeToProcess.getUrl(),
                pageResult.getNodes().size(),
                pageResult.getEdges().size(),
                pageResult.getStatusCode(),
                pageResult.getLoadTime(),
                pageResult.getRealTime()
        );

        return pageResult;
    }

    @Override
    public void run() {
        initQueueConsumer();
    }

    private void initQueueConsumer() {
        while (isWorking) {
            if (isPaused) {
                sleepSeconds(3);
            } else {
                processNext();
            }
        }
    }

    private void processNext() {
        if (isWorking) {
            Node nodeToProcess = crawlingProcessorService.crawlingStore().pullFromQueue();
            if (nodeToProcess != null) {
                crawlingProcessorService.stateStoreProcessor().nodeStarted(nodeToProcess);

                PageResult pageResult = processMessage(nodeToProcess);

                crawlingProcessorService.crawlingStoreProcessor().processCrawlerResult(pageResult);
                crawlingProcessorService.stateStoreProcessor().pageFinished(pageResult);

                if (!pageResult.isCache()) {
                    sleepSeconds(1);
                }
            } else {
                sleepSeconds(1);
            }

        }
    }
}