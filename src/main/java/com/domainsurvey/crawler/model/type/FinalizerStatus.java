package com.domainsurvey.crawler.model.type;

import lombok.Getter;

@Getter
public enum FinalizerStatus {
    NOT_COUNTING((byte) 1),
    UPDATE_NODE_COUNT((byte) 2),
    UPDATE_REDIRECTED_LINKS((byte) 3),
    COUNT_PAGE_RANK((byte) 4),
    IMPORT_INTO_FINAL_TABLES((byte) 5),
    FINALIZING((byte) 6),
    FINALIZED((byte) 7);

    private byte value;

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