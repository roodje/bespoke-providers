package com.yolt.providers.n26.common.dto.ais.consent;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

@ProjectedPayload
public interface ConsentStatusResponse {

    @JsonPath("$.consentStatus")
    String getConsentStatus();
}
