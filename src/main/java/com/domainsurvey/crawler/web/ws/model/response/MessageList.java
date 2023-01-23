package com.domainsurvey.crawler.web.ws.model.response;

import lombok.Getter;

import com.domainsurvey.crawler.service.dao.painator.model.PaginatorResult;
import com.domainsurvey.crawler.web.ws.model.request.message.DomainPaginationRequest;
import com.domainsurvey.crawler.web.ws.model.type.MessageType;

@Getter
public class MessageList<T> extends AbstractMessage {

    public MessageType messageType;
    PaginatorResult<T> result;
    DomainPaginationRequest domainPaginationRequest;

    public MessageList(String id, MessageType messageType, PaginatorResult<T> result, DomainPaginationRequest domainPaginationRequest) {
        super(id);
        this.result = result;
        this.messageType = messageType;
        this.domainPaginationRequest = domainPaginationRequest;
    }
}