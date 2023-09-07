package com.yolt.providers.ing.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InstructedAmount {

    private final String amount;
    private final String currency;
}
