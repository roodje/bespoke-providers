package com.yolt.providers.deutschebank.common.domain.model.consent;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

@ProjectedPayload
public interface ConsentStatusResponse {

    @JsonPath("$.consentStatus")
    String getConsentStatus();
}
