package com.domainsurvey.crawler.exception;

public class DomainQueueEmptyException extends Exception {

    public DomainQueueEmptyException() {
        super("no domains in queue");
    }
}
