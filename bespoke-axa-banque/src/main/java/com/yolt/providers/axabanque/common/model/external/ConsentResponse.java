package com.yolt.providers.axabanque.common.model.external;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

@ProjectedPayload
public interface ConsentResponse {

    @JsonPath("$.consentId")
    String getConsentId();

    @JsonPath("$._consentStatus")
    String getConsentStatus();
}
