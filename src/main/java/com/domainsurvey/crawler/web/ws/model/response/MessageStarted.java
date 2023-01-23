package com.domainsurvey.crawler.web.ws.model.response;

import lombok.Getter;

import com.domainsurvey.crawler.web.ws.model.type.MessageType;

@Getter
public class MessageStarted extends AbstractMessage {

    MessageType messageType = MessageType.PROCESS_STARTED;

    public MessageStarted(String id) {
        super(id);
    }
}