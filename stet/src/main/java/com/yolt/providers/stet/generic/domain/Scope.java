package com.yolt.providers.stet.generic.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Scope {

    AISP("aisp"),
    PISP("pisp"),
    AISP_PISP("aisp pisp"),
    AISP_EXTENDED_TRANSACTION_HISTORY("aisp extended_transaction_history");

    private String value;
}
