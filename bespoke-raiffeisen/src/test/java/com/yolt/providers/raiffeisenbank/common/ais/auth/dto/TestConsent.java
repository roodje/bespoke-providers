package com.yolt.providers.raiffeisenbank.common.ais.auth.dto;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class TestConsent implements Consent {

    private String consentId;

    @Override
    public String getConsentId() {
        return consentId;
    }
}
