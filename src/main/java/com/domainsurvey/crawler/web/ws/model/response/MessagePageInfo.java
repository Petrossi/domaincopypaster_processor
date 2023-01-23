package com.domainsurvey.crawler.web.ws.model.response;

import lombok.Getter;

import com.domainsurvey.crawler.dto.PageData;
import com.domainsurvey.crawler.web.ws.model.type.MessageType;

@Getter
public class MessagePageInfo extends AbstractMessage {
    PageData pageData;
    MessageType messageType = MessageType.PAGE_INFO;

    public MessagePageInfo(String id, PageData pageData) {
        super(id);

        this.pageData = pageData;
    }
}