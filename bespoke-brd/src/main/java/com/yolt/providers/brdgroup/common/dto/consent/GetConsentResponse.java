package com.yolt.providers.brdgroup.common.dto.consent;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

@ProjectedPayload
public interface GetConsentResponse {

    @JsonPath("$.consentStatus")
    String getConsentStatus();
}
