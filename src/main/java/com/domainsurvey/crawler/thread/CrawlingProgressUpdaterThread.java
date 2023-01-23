package com.domainsurvey.crawler.thread;

public class CrawlingProgressUpdaterThread extends Thread {
    public CrawlingProgressUpdaterThread(Runnable target) {
        super(target);
        setDaemon(true);
    }
}