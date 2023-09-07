package com.yolt.providers.stet.bnpparibasgroup.common.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Gets or Sets grantTypes
 */
public enum GrantTypesEnum {
    AUTHORIZATION_CODE("authorization_code"),

    CLIENT_CREDENTIALS("client_credentials"),

    REFRESH_TOKEN("refresh_token"),

    PASSWORD("password");

    private final String value;

    GrantTypesEnum(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }

    @JsonCreator
    public static GrantTypesEnum fromValue(String text) {
        for (GrantTypesEnum b : GrantTypesEnum.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + text + "'");
    }
}
