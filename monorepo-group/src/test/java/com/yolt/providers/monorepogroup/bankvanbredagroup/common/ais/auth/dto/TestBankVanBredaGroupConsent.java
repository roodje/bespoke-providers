package com.yolt.providers.monorepogroup.bankvanbredagroup.common.ais.auth.dto;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class TestBankVanBredaGroupConsent implements BankVanBredaGroupConsent {

    private final String consentId;
    private final String scaOAuth;

    @Override
    public String getConsentId() {
        return consentId;
    }

    @Override
    public String scaOAuth() {
        return scaOAuth;
    }
}
