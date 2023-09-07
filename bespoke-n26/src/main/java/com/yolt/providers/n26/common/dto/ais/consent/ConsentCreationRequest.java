package com.yolt.providers.n26.common.dto.ais.consent;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConsentCreationRequest {

    private Access access;
    private Boolean recurringIndicator;
    private String validUntil;
    private Integer frequencyPerDay;
}
