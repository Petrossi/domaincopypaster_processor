package com.domainsurvey.crawler.service.table.type;

import lombok.Getter;

@Getter
public enum SchemaType {

    PROCESS("process"),
    FINAL("final"),
    LAST("last"),
    MONITORING("monitoring"),
    PUBLIC("public");

    private String value;

    SchemaType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}