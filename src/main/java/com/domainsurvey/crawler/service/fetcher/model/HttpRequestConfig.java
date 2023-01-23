package com.domainsurvey.crawler.service.fetcher.model;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

public class HttpRequestConfig {
    public RequestConfig config;
    public HttpClientBuilder httpClientBuilder;
    public CloseableHttpClient httpClient = null;
    public CloseableHttpResponse response = null;
    public HttpRequestConfig(RequestConfig config, HttpClientBuilder httpClientBuilder) {
        this.config = config;
        this.httpClientBuilder = httpClientBuilder.setDefaultRequestConfig(config).disableAutomaticRetries();
    }
}
