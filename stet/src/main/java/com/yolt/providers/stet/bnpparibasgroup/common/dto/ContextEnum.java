package com.yolt.providers.stet.bnpparibasgroup.common.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Define the client context
 */
public enum ContextEnum {
    PSD2("psd2");

    private final String value;

    ContextEnum(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }

    @JsonCreator
    public static ContextEnum fromValue(String text) {
        for (ContextEnum b : ContextEnum.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + text + "'");
    }
}
