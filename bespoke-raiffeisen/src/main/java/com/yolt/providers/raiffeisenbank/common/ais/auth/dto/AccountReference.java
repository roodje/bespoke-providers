package com.yolt.providers.raiffeisenbank.common.ais.auth.dto;

import lombok.Getter;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;

@Getter
public class AccountReference {
    private String iban;
    public AccountReference(String iban) {
        this.iban = iban;
    }
}
