package com.domainsurvey.crawler.exception;

public class MaxRedirectCountException extends Exception {

    public MaxRedirectCountException() {
        super("max redirect count");
    }
}
