package com.domainsurvey.crawler.service.crawler.store.model;

import static com.domainsurvey.crawler.utils.Utils.getCurrentTimestamp;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import com.domainsurvey.crawler.dto.PageData;
import com.domainsurvey.crawler.service.crawler.model.PageResult;
import com.domainsurvey.crawler.service.filter.FilterParserService;

public class StateStore {
    private static int MAX_PAGES_LIMIT = 10;

    public long total;
    public long queue;
    public long finished;
    public long totalPage;
    public long queuePage;
    public long finishedPage;
    public long progress;
    public long blocked;
    public long error;
    public long warning;
    public long notice;
    public long noIssue;
    public long finishedImporting;
    public double totalScore;
    public byte score;
    public boolean filtersEnabled = true;
    public Timestamp crawlingStartedTimestamp;
    public Timestamp lastPageTimestamp;

    public Map<Integer, Long> filters = new HashMap<>();
    public BlockingQueue<PageData> lastCrawledPages = new LinkedBlockingDeque<>(MAX_PAGES_LIMIT);

    public void refreshIssuesFilters() {
        filters.put(FilterParserService.ALL_PAGES_FILTER_ID, totalPage);
        filters.put(FilterParserService.NO_ISSUE_FILTER_ID, noIssue);
        filters.put(FilterParserService.ANY_ISSUE_ID, totalPage - noIssue);
        filters.put(FilterParserService.ERROR_ISSUE_FILTER_ID, error);
        filters.put(FilterParserService.WARNING_ISSUE_FILTER_ID, warning);
        filters.put(FilterParserService.NOTICE_ISSUE_FILTER_ID, notice);
    }

    public void addCrawledPage(PageResult pageResult) {
        if (pageResult.isError() || pageResult.isNotice() || pageResult.isWarning()) {
            if (lastCrawledPages.size() == MAX_PAGES_LIMIT) {
                lastCrawledPages.poll();
            }
            lastCrawledPages.add(new PageData(pageResult));
        }

        lastPageTimestamp = getCurrentTimestamp();
    }
}