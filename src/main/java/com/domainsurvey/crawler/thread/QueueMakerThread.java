package com.domainsurvey.crawler.thread;

public class QueueMakerThread extends Thread {
    public QueueMakerThread(Runnable target) {
        super(target);
        setDaemon(true);
    }
}