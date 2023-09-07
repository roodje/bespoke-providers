package com.yolt.providers.monorepogroup.raiffeisenatgroup.common.dto.external;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum ConsentStatus {

    RECEIVED("received"),
    REJECTED("rejected"),
    VALID("valid"),
    REVOKED_BY_PSU("revokedByPsu"),
    EXPIRED("expired"),
    TERMINATED_BY_TPP("terminatedByTpp");

    private final String value;

    ConsentStatus(String value) {
        this.value = value;
    }

    @JsonCreator
    public static ConsentStatus fromValue(String text) {
        for (ConsentStatus cs : ConsentStatus.values()) {
            if (cs.value.equals(text)) {
                return cs;
            }
        }
        return null;
    }
}
