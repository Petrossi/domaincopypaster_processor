package com.domainsurvey.crawler.model.type;

import lombok.Getter;

@Getter
public enum NodeType {
    INTERNAL((byte) 1),
    EXTERNAL((byte) 2),
    CSS((byte) 3),
    JS((byte) 4),
    IMAGE((byte) 5);

    private byte value;

    NodeType(byte value) {
        this.value = value;
    }

    public static NodeType fromValue(Byte value) {
        for (NodeType finalizerStatus : values()) {
            if (finalizerStatus.getValue() == value) {
                return finalizerStatus;
            }
        }
        return null;
    }
}