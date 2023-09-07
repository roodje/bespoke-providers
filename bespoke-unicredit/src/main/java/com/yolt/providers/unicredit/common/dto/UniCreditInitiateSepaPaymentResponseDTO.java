package com.yolt.providers.unicredit.common.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

@ProjectedPayload
@JsonIgnoreProperties(ignoreUnknown = true)
public interface UniCreditInitiateSepaPaymentResponseDTO {

    @JsonPath("$.transactionStatus")
    String getStatus();

    @JsonPath("$.paymentId")
    String getPaymentId();

    @JsonPath("$._links.scaRedirect.href")
    String getRedirectUrl();
}
