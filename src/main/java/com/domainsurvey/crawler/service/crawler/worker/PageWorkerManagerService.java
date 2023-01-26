package com.domainsurvey.crawler.service.crawler.worker;

import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import com.domainsurvey.crawler.model.domain.Domain;
import com.domainsurvey.crawler.service.crawler.processor.CrawlingProcessorService;

@Service
@Scope("prototype")
@Log4j2
public class PageWorkerManagerService {

    private static final int DEFAULT_THREAD_COUNT = 3;

    @Autowired
    private BeanFactory beanFactory;

    private Domain domain;
    private CrawlingProcessorService crawlingProcessorService;

    private int threadCount = DEFAULT_THREAD_COUNT;

    private final List<PageWorkerService> workers = new ArrayList<>();

    private volatile boolean inited;
    private volatile boolean isRunning;

    public PageWorkerManagerService(CrawlingProcessorService crawlingProcessorService) {
        this.crawlingProcessorService = crawlingProcessorService;
        this.domain = crawlingProcessorService.domain();
        this.threadCount = domain.getConfig().getThreadCount();

        if (this.threadCount > 20) {
            this.threadCount = 20;
        }
    }

    public PageWorkerManagerService() {

    }

    public void stop() {
        isRunning = false;
        log.info("stop {}: | {}", domain.getId(), threadCount);

        workers.forEach(PageWorkerService::stop);
    }

    public void start() {
        if (isRunning) {
            log.info("{} is already running", domain.getId());

            return;
        }

        if (!inited) {
            log.info("init: {}", domain.getId());

            IntStream.range(0, threadCount).forEach(i -> createReceiverForDomain());

            inited = true;
        }

        isRunning = true;

        log.info("start : {}  | {}", domain.getId(), threadCount);

        workers.forEach(PageWorkerService::start);
    }

    private synchronized void createReceiverForDomain(){
        PageWorkerService urlWorkerService = beanFactory.getBean(PageWorkerService.class, crawlingProcessorService);

        workers.add(urlWorkerService);
    }
}