package com.yolt.providers.openbanking.ais.common.enums;

public enum ResponseTypes {
    CODE("code"),
    ID_TOKEN("id_token");

    private String value;

    ResponseTypes(final String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.valueOf(this.value);
    }
}
