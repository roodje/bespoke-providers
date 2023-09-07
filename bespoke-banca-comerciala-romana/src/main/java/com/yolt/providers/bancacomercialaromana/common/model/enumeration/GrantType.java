package com.yolt.providers.bancacomercialaromana.common.model.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum GrantType {
    AUTHORIZATION_CODE("authorization_code"),

    CLIENT_CREDENTIALS("client_credentials"),

    REFRESH_TOKEN("refresh_token");

    private String value;

    @Override
    public String toString() {
        return value;
    }
}
