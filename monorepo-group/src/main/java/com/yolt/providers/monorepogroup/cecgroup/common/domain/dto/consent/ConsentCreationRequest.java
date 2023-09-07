package com.yolt.providers.monorepogroup.cecgroup.common.domain.dto.consent;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class ConsentCreationRequest implements Serializable {

    private Access access;
    private Boolean recurringIndicator;
    private String validUntil;
    private Boolean combinedServiceIndicator;
    private Integer frequencyPerDay;
}
