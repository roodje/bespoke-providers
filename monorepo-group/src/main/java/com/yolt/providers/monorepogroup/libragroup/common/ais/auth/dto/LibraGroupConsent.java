package com.yolt.providers.monorepogroup.libragroup.common.ais.auth.dto;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

@ProjectedPayload
public interface LibraGroupConsent {
    @JsonPath("$.consentId")
    String getConsentId();
}
