package com.domainsurvey.crawler.model.type;

import lombok.Getter;

@Getter
public enum CommonVersion {

    NEW((byte) 1),
    OLD((byte) 2),
    ARCHIVE((byte) 3);

    private byte value;

    CommonVersion(byte value) {
        this.value = value;
    }

    public static CommonVersion fromValue(Byte value) {
        for (CommonVersion commonVersion : values()) {
            if (commonVersion.getValue() == value) {
                return commonVersion;
            }
        }
        return null;
    }
}