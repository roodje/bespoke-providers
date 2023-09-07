package com.yolt.providers.fineco.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yolt.providers.common.pis.sepa.SepaLinksDTO;
import lombok.Value;

@Value
public class PaymentResponse {

    private String transactionStatus;
    private String paymentId;

    @JsonProperty("_links")
    private SepaLinksDTO links;


}
