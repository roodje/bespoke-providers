package com.yolt.providers.rabobank.dto.external;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Gets or Sets balanceType
 */
public enum BalanceType {

    EXPECTED("expected");

    private final String value;

    BalanceType(String value) {
        this.value = value;
    }

    @JsonCreator
    public static BalanceType fromValue(String text) {
        for (BalanceType b : BalanceType.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + text + "'");
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }
}

