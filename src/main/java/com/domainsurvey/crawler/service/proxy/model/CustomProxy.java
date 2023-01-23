package com.domainsurvey.crawler.service.proxy.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.net.InetSocketAddress;
import java.net.Proxy;

import org.apache.http.HttpHost;

@Data
@AllArgsConstructor
public class CustomProxy implements CustomHttpProxyInterface{
    private String host;
    private byte port;

    public HttpHost toApacheProxy() {
        return new HttpHost(getHost(), getPort());
    }
}