package com.yolt.providers.stet.generic.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StetChargeBearer {

    DEBT("DEBT"),
    CRED("CRED"),
    SHAR("SHAR"),
    SLEV("SLEV");

    private final String value;
}