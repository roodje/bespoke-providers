package com.yolt.providers.raiffeisenbank.common.ais.auth.dto;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

@ProjectedPayload
public interface Consent {
    @JsonPath("$.consentId")
    String getConsentId();
}
