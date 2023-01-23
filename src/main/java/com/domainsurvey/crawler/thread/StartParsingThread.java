package com.domainsurvey.crawler.thread;

public class StartParsingThread extends Thread {
    public StartParsingThread(Runnable target) {
        super(target);
    }
}
