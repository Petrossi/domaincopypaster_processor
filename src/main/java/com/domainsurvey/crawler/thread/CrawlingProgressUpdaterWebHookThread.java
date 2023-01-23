package com.domainsurvey.crawler.thread;

public class CrawlingProgressUpdaterWebHookThread extends Thread {
    public CrawlingProgressUpdaterWebHookThread(Runnable target) {
        super(target);
        setDaemon(true);
    }
}