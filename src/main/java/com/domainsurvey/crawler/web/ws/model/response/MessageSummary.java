package com.domainsurvey.crawler.web.ws.model.response;

import lombok.Getter;

import java.util.List;

import com.domainsurvey.crawler.model.domain.DomainCrawlingInfo;
import com.domainsurvey.crawler.model.filter.DomainFilter;
import com.domainsurvey.crawler.web.ws.model.type.MessageType;

@Getter
public class MessageSummary extends AbstractMessage {
    DomainCrawlingInfo crawlingInfo;
    List<DomainFilter> filters;
    MessageType messageType = MessageType.SUMMARY;

    public MessageSummary(String token, DomainCrawlingInfo crawlingInfo, List<DomainFilter> filters) {
        super(token);

        this.crawlingInfo = crawlingInfo;
        this.filters = filters;
    }
}