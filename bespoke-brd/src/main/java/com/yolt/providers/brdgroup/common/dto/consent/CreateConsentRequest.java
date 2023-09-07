package com.yolt.providers.brdgroup.common.dto.consent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CreateConsentRequest {

    private Access access;
    private Boolean recurringIndicator;
    private String validUntil;
    private Boolean combinedServiceIndicator;
    private Integer frequencyPerDay;
}
