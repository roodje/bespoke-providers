package com.yolt.providers.rabobank.dto.external;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * The transaction status is filled with codes of the ISO 20022 data table
 */
public enum TransactionStatus {

    ACCC("ACCC"),

    ACCP("ACCP"),

    ACSC("ACSC"),

    ACSP("ACSP"),

    ACTC("ACTC"),

    ACWC("ACWC"),

    ACWP("ACWP"),

    RCVD("RCVD"),

    PDNG("PDNG"),

    RJCT("RJCT"),

    CANC("CANC"),

    ACFC("ACFC"),

    PATC("PATC"),

    PART("PART");

    private final String value;

    TransactionStatus(String value) {
        this.value = value;
    }

    @JsonCreator
    public static TransactionStatus fromValue(String text) {
        for (TransactionStatus b : TransactionStatus.values()) {
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

