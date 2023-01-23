package com.domainsurvey.crawler.service.proxy.model;

import org.apache.http.HttpHost;

public interface CustomHttpProxyInterface {
    String getHost();
    byte getPort();
    HttpHost toApacheProxy();
}
