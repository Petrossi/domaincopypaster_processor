package com.domainsurvey.crawler.service.crawler.queueMaker;

import java.util.concurrent.BlockingQueue;

import com.domainsurvey.crawler.model.page.Page;

public interface QueueMakerProcessorService {
    void start();

    void stop();

    BlockingQueue<Page> getQueue();
}