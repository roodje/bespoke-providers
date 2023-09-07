package com.yolt.providers.monorepogroup.handelsbankengroup.common.domain.dto;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

@ProjectedPayload
public interface ConsentResponse {

    @JsonPath("$.consentId")
    String getConsentId();
}
