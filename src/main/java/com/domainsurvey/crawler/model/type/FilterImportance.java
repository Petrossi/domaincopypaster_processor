package com.domainsurvey.crawler.model.type;

import lombok.Getter;

import java.util.List;

import com.google.common.collect.ImmutableList;

@Getter
public enum FilterImportance {

    ISSUE((byte) 0),
    ERROR((byte) 4),
    WARNING((byte) 5),
    NOTICE((byte) 6),
    INFO((byte) 4);

    public static final List<FilterImportance> ISSUES_TYPES = ImmutableList.of(
            FilterImportance.NOTICE,
            FilterImportance.WARNING,
            FilterImportance.ERROR
    );

    private byte value;

    FilterImportance(byte value) {
        this.value = value;
    }

    public static FilterImportance fromValue(Byte value) {
        for (FilterImportance filterImportance : values()) {
            if (filterImportance.getValue() == value) {
                return filterImportance;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}