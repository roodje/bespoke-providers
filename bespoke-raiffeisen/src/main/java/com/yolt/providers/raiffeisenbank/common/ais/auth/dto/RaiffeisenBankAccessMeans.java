package com.yolt.providers.raiffeisenbank.common.ais.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RaiffeisenBankAccessMeans {

    private RaiffeisenBankTokens tokens;
    private String consentId;
}