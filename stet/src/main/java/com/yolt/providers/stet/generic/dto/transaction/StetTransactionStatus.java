package com.yolt.providers.stet.generic.dto.transaction;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Known in STET as TransactionStatus
 */
@Getter
@AllArgsConstructor
public enum StetTransactionStatus {

    BOOK("BOOK"),
    PDNG("PDNG"),
    FUTR("FUTR"),
    INFO("INFO"),
    OTHR("OTHR"),
    RJCT("RJCT");

    private final String value;
}
