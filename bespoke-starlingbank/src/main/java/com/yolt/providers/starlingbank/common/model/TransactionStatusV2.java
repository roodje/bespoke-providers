package com.yolt.providers.starlingbank.common.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TransactionStatusV2 {

    UPCOMING("UPCOMING"),
    PENDING("PENDING"),
    REVERSED("REVERSED"),
    SETTLED("SETTLED"),
    DECLINED("DECLINED"),
    REFUNDED("REFUNDED"),
    RETRYING("RETRYING"),
    ACCOUNT_CHECK("ACCOUNT_CHECK");

    private String value;
}
