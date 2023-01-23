package com.domainsurvey.crawler.service.fetcher.model;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.Objects;

@Data
@Builder
public class HttpConfig implements Serializable {

    public static final String GET_REQUEST = "GET";
    public static final String POST_REQUEST = "POST";
    public static int DEFAULT_TIMEOUT = 15;

    public static final String DESCRIPTOR_METHOD = "descriptor";
    public static final String PHANTOM_METHOD = "phantom";
    public static final String HTTP_METHOD = "http";

    public String url;
    @Builder.Default
    public String type = GET_REQUEST;
    public boolean onlyHeaders;
    public boolean followRedirect;
    @Builder.Default
    public int maxRetries = 1;
    public boolean proxy;
    @Builder.Default
    public String contentType = null;
    @Builder.Default
    public String requestData = null;
    @Builder.Default
    public String serviceType = "http";
    @Builder.Default
    public int timeout = DEFAULT_TIMEOUT;
    public boolean retryAtBadStatusCode;
    public boolean clearHtml;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HttpConfig)) return false;
        HttpConfig that = (HttpConfig) o;
        return isOnlyHeaders() == that.isOnlyHeaders() &&
                isFollowRedirect() == that.isFollowRedirect() &&
                Objects.equals(getUrl(), that.getUrl()) &&
                Objects.equals(getType(), that.getType()) &&
                Objects.equals(getContentType(), that.getContentType()) &&
                Objects.equals(getRequestData(), that.getRequestData());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUrl(), getType(), isOnlyHeaders(), isFollowRedirect(), getContentType(), getRequestData());
    }
}