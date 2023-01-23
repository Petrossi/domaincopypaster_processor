package com.domainsurvey.crawler.service.crawler.queueMaker.impl;

import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import com.domainsurvey.crawler.model.domain.Domain;
import com.domainsurvey.crawler.model.page.Page;
import com.domainsurvey.crawler.service.crawler.processor.CrawlingProcessorService;
import com.domainsurvey.crawler.service.crawler.queueMaker.QueueMakerProcessorService;

@Service
@Scope("prototype")
@NoArgsConstructor
@Log4j2
public class QueueMakerProcessorServiceImpl implements QueueMakerProcessorService {

    private boolean isRunning = false;
    private boolean isPaused = true;

    private Domain domain;

    public BlockingQueue<Page> queue = new LinkedBlockingDeque<>(10000);

    public QueueMakerProcessorServiceImpl(CrawlingProcessorService crawlingProcessorService) {
        this.domain = crawlingProcessorService.domain();
    }

    public void start() {
        if (isRunning && !isPaused) {
            log.info("already in process : {}", domain.getId());
            return;
        }
        log.info("start: {}" + domain.getId());

        isRunning = true;
        isPaused = false;
    }

    public void stop() {
        isRunning = false;

        log.info("stopped : {}", domain.getId());
    }

    @Override
    public BlockingQueue<Page> getQueue() {
        return queue;
    }
}