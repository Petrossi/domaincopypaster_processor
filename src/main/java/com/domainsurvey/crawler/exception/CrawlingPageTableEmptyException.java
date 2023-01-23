package com.domainsurvey.crawler.exception;

public class CrawlingPageTableEmptyException extends Exception {
    public CrawlingPageTableEmptyException(String id, String message) {
        super(id + ": " + message);
    }
}
