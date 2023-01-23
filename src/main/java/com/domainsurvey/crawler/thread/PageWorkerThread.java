package com.domainsurvey.crawler.thread;

public class PageWorkerThread extends Thread {
    public PageWorkerThread(Runnable target) {
        super(target);
    }
}
