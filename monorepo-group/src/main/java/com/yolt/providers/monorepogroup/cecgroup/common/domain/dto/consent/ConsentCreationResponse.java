package com.yolt.providers.monorepogroup.cecgroup.common.domain.dto.consent;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

@ProjectedPayload
public interface ConsentCreationResponse {

    @JsonPath("$.consentId")
    String getConsentId();
}
