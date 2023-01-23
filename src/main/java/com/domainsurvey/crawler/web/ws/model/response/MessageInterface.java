package com.domainsurvey.crawler.web.ws.model.response;

import com.domainsurvey.crawler.web.ws.model.type.MessageType;

public interface MessageInterface {
    MessageType getMessageType();
    String getId();
}