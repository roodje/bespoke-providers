package com.yolt.providers.triodosbank.common.model.http;

import com.yolt.providers.triodosbank.common.model.Access;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConsentCreationRequest {

    @Builder.Default
    private Access access = new Access();
    private Boolean recurringIndicator;
    private String validUntil;
    private Integer frequencyPerDay;
    private Boolean combinedServiceIndicator;
}
