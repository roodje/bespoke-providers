package com.yolt.providers.openbanking.ais.common.enums;

public enum GrantTypes {
    AUTHORIZATION_CODE("authorization_code"),
    CLIENT_CREDENTIALS("client_credentials"),
    REFRESH_TOKEN("refresh_token");

    private String value;

    GrantTypes(final String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}
