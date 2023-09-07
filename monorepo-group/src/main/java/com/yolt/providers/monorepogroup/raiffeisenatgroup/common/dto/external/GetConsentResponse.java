package com.yolt.providers.monorepogroup.raiffeisenatgroup.common.dto.external;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

import java.time.LocalDate;

@ProjectedPayload
public interface GetConsentResponse {
    @JsonPath("$.consentStatus")
    ConsentStatus getConsentStatus();

    @JsonPath("$.validUntil")
    LocalDate getValidUntil();
}
