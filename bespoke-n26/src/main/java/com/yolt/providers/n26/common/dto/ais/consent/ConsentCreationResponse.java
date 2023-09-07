package com.yolt.providers.n26.common.dto.ais.consent;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

@ProjectedPayload
public interface ConsentCreationResponse {

    @JsonPath("$.consentId")
    String getConsentId();

    @JsonPath("$.consentStatus")
    String getConsentStatus();

    @JsonPath("$._links.status.href")
    String getStatusHref();

}
