package com.yolt.providers.gruppocedacri.common.dto.consent;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

@ProjectedPayload
public interface ConsentResponse {

    @JsonPath("$.consentId")
    String getConsentId();

    @JsonPath("$.consentStatus")
    String getConsentStatus();

    @JsonPath("$._links.scaRedirect.href")
    String getScaRedirect();
}
