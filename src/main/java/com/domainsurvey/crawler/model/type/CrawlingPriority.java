package com.domainsurvey.crawler.model.type;

import lombok.Getter;

@Getter
public enum CrawlingPriority {

    MONITORING((byte) 10),
    NEW((byte) 1),
    RESTART((byte) 2);

    private byte value;

    CrawlingPriority(byte value) {
        this.value = value;
    }

    public static CrawlingPriority fromValue(Byte value) {
        for (CrawlingPriority crawlingPriority : values()) {
            if (crawlingPriority.getValue() == value) {
                return crawlingPriority;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}