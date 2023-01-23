package com.domainsurvey.crawler.model.type;

import lombok.Getter;

@Getter
public enum CrawlingStatus {

    CRAWLING((byte) 1),
    FINISHED((byte) 2),
    QUEUE((byte) 3),
    WAITING_FOR_FINALIZE((byte) 4),
    WAITING_FINALIZE_IN_PROGRESS((byte) 5),
    FINALIZING_FAILED((byte) 6),
    DELETED((byte) 7),
    STARTING_FAILED((byte) 8),
    CREATED((byte) 9),
    RE_CRAWLING((byte) 10);

    private byte value;

    CrawlingStatus(byte value) {
        this.value = value;
    }

    public static CrawlingStatus fromValue(Byte value) {
        for (CrawlingStatus crawlingStatus : values()) {
            if (crawlingStatus.getValue() == value) {
                return crawlingStatus;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}