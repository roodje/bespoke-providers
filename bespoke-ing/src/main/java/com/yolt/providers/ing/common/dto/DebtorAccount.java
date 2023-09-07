package com.yolt.providers.ing.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DebtorAccount {

    private final String currency;
    private final String iban;
}
