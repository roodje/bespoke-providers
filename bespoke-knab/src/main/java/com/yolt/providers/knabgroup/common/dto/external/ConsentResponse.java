package com.yolt.providers.knabgroup.common.dto.external;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

@ProjectedPayload
public interface ConsentResponse {

    @JsonPath("$.consentId")
    String getConsentId();

}
