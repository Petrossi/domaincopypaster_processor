package com.domainsurvey.crawler.model.type;

import lombok.Getter;

@Getter
public enum FinalizerStatus {
    NOT_COUNTING((byte) 1),
    UPDATE_NODE_COUNT((byte) 2),
    UPDATE_REDIRECTED_LINKS((byte) 3),
    RESOURCE_DOWNLOADER((byte) 4),
    COUNT_PAGE_RANK((byte) 5),
    IMPORT_INTO_FINAL_TABLES((byte) 6),
    FINALIZING((byte) 7),
    FINALIZED((byte) 8);

    private final byte value;

    FinalizerStatus(byte value) {
        this.value = value;
    }

    public static FinalizerStatus fromValue(Byte value) {
        for (FinalizerStatus finalizerStatus : values()) {
            if (finalizerStatus.getValue() == value) {
                return finalizerStatus;
            }
        }
        return null;
    }
}