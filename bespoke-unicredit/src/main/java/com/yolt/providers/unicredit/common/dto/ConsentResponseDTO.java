package com.yolt.providers.unicredit.common.dto;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

@ProjectedPayload
public interface ConsentResponseDTO {

    @JsonPath("$.consentId")
    String getConsentId();

    @JsonPath("$._links.scaRedirect.href")
    String getConsentUrl();
}
