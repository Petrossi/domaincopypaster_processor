package com.domainsurvey.crawler.thread;

public class CrawlingStarterProcessStartingThread extends Thread {
    public CrawlingStarterProcessStartingThread(Runnable target) {
        super(target);
    }
}