package com.domainsurvey.crawler.web.ws.model.response;

import lombok.Getter;

import static com.domainsurvey.crawler.utils.Utils.getCurrentTimestamp;

import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;

import com.domainsurvey.crawler.dto.PageData;
import com.domainsurvey.crawler.model.filter.DomainFilter;
import com.domainsurvey.crawler.service.crawler.store.model.StateStore;
import com.domainsurvey.crawler.web.ws.model.type.MessageType;

@Getter
public class MessageProcessUpdate extends AbstractMessage {
    long total;
    long queue;
    long progress;
    long finished;
    long error;
    long warning;
    long notice;
    long noIssue;
    byte score;
    MessageType messageType = MessageType.PROCESS_UPDATE;
    public List<DomainFilter> filters;
    public BlockingQueue<PageData> lastCrawledPages;
    private Timestamp crawlingStartedTimestamp;
    private Timestamp crawlingFinishedTimestamp;
    private double pagesPerSeconds;
    public long crawlingTimeSeconds;

    public MessageProcessUpdate(String token, StateStore stateStore) {
        super(token);
        this.total = stateStore.totalPage;
        this.queue = stateStore.queuePage;
        this.progress = stateStore.progress;
        this.finished = stateStore.finishedPage;
        this.error = stateStore.error;
        this.warning = stateStore.warning;
        this.notice = stateStore.notice;
        this.noIssue = stateStore.noIssue;
        this.filters = stateStore.filters.entrySet().stream().map((e) -> {
            DomainFilter domainFilter = new DomainFilter();
            domainFilter.setFilterId(e.getKey());
            domainFilter.setCount(e.getValue().intValue());
            return domainFilter;
        }).collect(Collectors.toList());
        this.score = stateStore.score;
        this.lastCrawledPages = stateStore.lastCrawledPages;
        this.crawlingStartedTimestamp = stateStore.crawlingStartedTimestamp;
        crawlingFinishedTimestamp = getCurrentTimestamp();

        if (stateStore.lastPageTimestamp != null) {
            crawlingTimeSeconds = (crawlingStartedTimestamp.getTime() / 1000) - (stateStore.lastPageTimestamp.getTime() / 1000);

            pagesPerSeconds = (double) finished / crawlingTimeSeconds;
        }
    }
}