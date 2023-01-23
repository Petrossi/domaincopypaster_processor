package com.domainsurvey.crawler.service.proxy;

import com.domainsurvey.crawler.service.proxy.model.CustomHttpProxyInterface;
import org.apache.http.HttpHost;

public interface ProxyService {
    CustomHttpProxyInterface getRandomProxy();
    HttpHost getApacheRandomProxy();
}