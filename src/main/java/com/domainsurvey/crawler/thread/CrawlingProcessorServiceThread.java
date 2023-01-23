package com.domainsurvey.crawler.thread;

public class CrawlingProcessorServiceThread extends Thread {
    public CrawlingProcessorServiceThread(Runnable target) {
        super(target);
    }
}
