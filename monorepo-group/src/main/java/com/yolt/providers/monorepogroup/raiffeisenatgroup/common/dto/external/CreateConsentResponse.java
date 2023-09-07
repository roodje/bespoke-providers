package com.yolt.providers.monorepogroup.raiffeisenatgroup.common.dto.external;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

@ProjectedPayload
public interface CreateConsentResponse {
    @JsonPath("$.consentId")
    String getConsentId();

    @JsonPath("$._links.scaRedirect.href")
    String getScaRedirect();

}
