package com.domainsurvey.crawler.web.ws.model.response;

import lombok.Getter;

@Getter
public abstract class AbstractMessage implements MessageInterface {
    String id;

    public AbstractMessage(String id) {
        this.id = id;
    }
}