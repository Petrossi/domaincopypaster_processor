package com.domainsurvey.crawler.exception;

public class MaxDomainsParsingLimitException extends Exception {

    public MaxDomainsParsingLimitException() {
        super("MAX_PARSING_DOMAINS in domainStorage");
    }
}
