package com.yolt.providers.monorepogroup.raiffeisenatgroup.common.dto.external;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum RaiffeisenBalanceType {
    INTERIM_AVAILABLE("interimAvailable"),
    FORWARD_AVAILABLE("forwardAvailable");

    private String value;

    RaiffeisenBalanceType(String value) {
        this.value = value;
    }

    @JsonCreator
    public static RaiffeisenBalanceType fromValue(String text) {
        for (RaiffeisenBalanceType bt : RaiffeisenBalanceType.values()) {
            if (bt.value.equals(text)) {
                return bt;
            }
        }
        return null;
    }
}
