package com.domainsurvey.crawler.model.type;

import lombok.Getter;

@Getter
public enum DownloadStatus {

    PENDING((byte) 1),
    SUCCESS((byte) 2),
    FAILED((byte) 3);

    private final byte value;

    DownloadStatus(byte value) {
        this.value = value;
    }

    public static DownloadStatus fromValue(Byte value) {
        for (DownloadStatus crawlingStatus : values()) {
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