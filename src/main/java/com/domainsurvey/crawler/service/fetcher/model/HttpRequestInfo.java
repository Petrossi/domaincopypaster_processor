package com.domainsurvey.crawler.service.fetcher.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class HttpRequestInfo {
    public short statusCode;
    public long contentLength;
    public String contentType;
    public String proxy;
    public String location;
    public List<String> headers = new ArrayList<>();
}