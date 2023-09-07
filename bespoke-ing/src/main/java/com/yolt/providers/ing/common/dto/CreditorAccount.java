package com.yolt.providers.ing.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreditorAccount {

    private final String currency;
    private final String iban;
}
