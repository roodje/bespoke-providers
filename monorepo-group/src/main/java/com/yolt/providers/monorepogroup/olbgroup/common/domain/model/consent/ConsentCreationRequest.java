package com.yolt.providers.monorepogroup.olbgroup.common.domain.model.consent;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConsentCreationRequest {

    private Access access;
    private Boolean recurringIndicator;
    private String validUntil;
    private Boolean combinedServiceIndicator;
    private Integer frequencyPerDay;
}
