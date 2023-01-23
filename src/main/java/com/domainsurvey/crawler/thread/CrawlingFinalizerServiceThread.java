package com.domainsurvey.crawler.thread;

public class CrawlingFinalizerServiceThread extends Thread {
    public CrawlingFinalizerServiceThread(Runnable target) {
        super(target);
    }
}