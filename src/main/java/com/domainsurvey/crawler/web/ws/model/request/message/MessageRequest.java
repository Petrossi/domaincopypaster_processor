package com.domainsurvey.crawler.web.ws.model.request.message;

import lombok.Data;
import lombok.ToString;

import com.domainsurvey.crawler.web.ws.model.type.MessageType;

@ToString(callSuper = true)
@Data
public class MessageRequest {
    private MessageType messageType;
}