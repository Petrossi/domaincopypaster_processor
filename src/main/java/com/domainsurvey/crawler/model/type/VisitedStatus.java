package com.domainsurvey.crawler.model.type;

import lombok.Getter;

@Getter
public enum VisitedStatus {

    NOT_VISITED((byte) 0),
    VISITED((byte) 1),
    IN_PROGRESS((byte) 2);

    private byte value;

    VisitedStatus(byte value) {
        this.value = value;
    }

    public static VisitedStatus fromValue(Byte value) {
        for (VisitedStatus visitedStatus : values()) {
            if (visitedStatus.getValue() == value) {
                return visitedStatus;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}