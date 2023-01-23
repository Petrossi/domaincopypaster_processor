package com.domainsurvey.crawler.web.ws.model.response;

import lombok.Getter;

import com.domainsurvey.crawler.web.ws.model.type.MessageType;

@Getter
public class MessageFinished extends AbstractMessage {

    MessageType messageType =  MessageType.PROCESS_FINISHED;

    public MessageFinished(String id) {
        super(id);
    }
}