package com.domainsurvey.crawler.thread;

public class MonitoringServiceThread extends Thread {
    public MonitoringServiceThread(Runnable target) {
        super(target);
    }
}