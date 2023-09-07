package com.yolt.providers.monorepogroup.bankvanbredagroup.common.ais.auth.dto;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

@ProjectedPayload
public interface BankVanBredaGroupConsent {
    @JsonPath("$.consentId")
    String getConsentId();

    @JsonPath("$._links.scaOAuth.href")
    String scaOAuth();
}
