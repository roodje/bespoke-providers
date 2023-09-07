package com.yolt.providers.stet.bnpparibasgroup.common.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Requested authentication method for the token endpoint.
 */
public enum TokenEndpointAuthMethodEnum {
    TLS_CLIENT_AUTH("tls_client_auth");

    private final String value;

    TokenEndpointAuthMethodEnum(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }

    @JsonCreator
    public static TokenEndpointAuthMethodEnum fromValue(String text) {
        for (TokenEndpointAuthMethodEnum b : TokenEndpointAuthMethodEnum.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + text + "'");
    }
}
