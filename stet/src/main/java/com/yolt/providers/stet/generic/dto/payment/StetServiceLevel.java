package com.yolt.providers.stet.generic.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StetServiceLevel {

    SEPA("SEPA");

    private final String value;
}