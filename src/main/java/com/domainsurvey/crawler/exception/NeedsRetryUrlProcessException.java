package com.domainsurvey.crawler.exception;

import lombok.Getter;

@Getter
public class NeedsRetryUrlProcessException extends Exception {

    private boolean oneRetry;
    private short statusCode;

    public NeedsRetryUrlProcessException(boolean oneRetry, short statusCode) {
        super("Needs to retry url process -> " + statusCode);
        this.statusCode = statusCode;
        this.oneRetry = oneRetry;
    }
}
