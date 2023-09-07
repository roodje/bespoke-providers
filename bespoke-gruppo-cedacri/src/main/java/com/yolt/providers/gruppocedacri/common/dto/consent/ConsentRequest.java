package com.yolt.providers.gruppocedacri.common.dto.consent;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ConsentRequest {

    private ConsentAccess access;
    private Boolean recurringIndicator;
    private String validUntil;
    private Integer frequencyPerDay;
    private Boolean combinedServiceIndicator;
}
