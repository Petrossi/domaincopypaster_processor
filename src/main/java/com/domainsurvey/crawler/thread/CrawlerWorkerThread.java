package com.domainsurvey.crawler.thread;

public class CrawlerWorkerThread extends Thread {
    public CrawlerWorkerThread(Runnable target) {
        super(target);
    }
}
