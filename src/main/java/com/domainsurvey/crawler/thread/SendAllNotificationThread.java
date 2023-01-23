package com.domainsurvey.crawler.thread;

public class SendAllNotificationThread extends Thread {
    public SendAllNotificationThread(Runnable target) {
        super(target);
    }
}