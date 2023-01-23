package com.domainsurvey.crawler.exception;

import java.io.IOException;

public class BadHttpStatusCodeException extends IOException {

    public short statusCode;

    public BadHttpStatusCodeException(short statusCode) {
        this.statusCode = statusCode;
    }
}
