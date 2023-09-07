package com.yolt.providers.unicredit.common.dto;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

@ProjectedPayload
public interface UniCreditSepaPaymentStatusResponseDTO {

    @JsonPath("$.transactionStatus")
    String getStatus();
}
