package com.domainsurvey.crawler.service.backend.model;

import lombok.Data;

import com.domainsurvey.crawler.model.type.CrawlingStatus;

@Data
public class BackendDomain {
    private String id;
    private String host;
    private String protocol;
    private boolean ignoreRobots;
    private int pagesLimit;
    private byte threadCount;
    private CrawlingStatus status;
    private boolean reportSaved;
}