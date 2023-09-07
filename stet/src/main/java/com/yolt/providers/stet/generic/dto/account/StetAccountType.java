package com.yolt.providers.stet.generic.dto.account;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Known in STET as CashAccountTypeEnum
 */
@Getter
@AllArgsConstructor
public enum StetAccountType {

    CACC("CACC"),
    CARD("CARD");

    private final String value;
}