package com.yolt.providers.yoltprovider.pis.sepa.pecadapter.submit;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yolt.providers.common.pis.sepa.SepaPaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SepaPaymentStatusResponse {

    @JsonProperty("paymentId")
    private String paymentId;
    @JsonProperty("paymentStatus")
    private SepaPaymentStatus paymentStatus;
}