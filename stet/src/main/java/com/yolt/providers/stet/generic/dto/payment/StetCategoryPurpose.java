package com.yolt.providers.stet.generic.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StetCategoryPurpose {

    CASH("CASH"),
    CORT("CORT"),
    DVPM("DVPM"),
    INTC("INTC"),
    TREA("TREA");

    private final String value;
}