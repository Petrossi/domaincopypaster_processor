package com.domainsurvey.crawler.web.ws.model.response;

import lombok.Getter;

import com.domainsurvey.crawler.service.backend.model.BackendDomain;
import com.domainsurvey.crawler.web.ws.model.type.MessageType;

@Getter
public class MessageDomainInfo extends AbstractMessage {
    BackendDomain domain;
    MessageType messageType = MessageType.DOMAIN_INFO;

    public MessageDomainInfo(String id, BackendDomain domain) {
        super(id);

        this.domain = domain;
    }
}