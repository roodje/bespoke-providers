package com.yolt.providers.stet.bnpparibasgroup.common.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Gets or Sets responseTypes
 */
public enum ResponseTypesEnum {
    CODE("code");

    private final String value;

    ResponseTypesEnum(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }

    @JsonCreator
    public static ResponseTypesEnum fromValue(String text) {
        for (ResponseTypesEnum b : ResponseTypesEnum.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + text + "'");
    }
}
