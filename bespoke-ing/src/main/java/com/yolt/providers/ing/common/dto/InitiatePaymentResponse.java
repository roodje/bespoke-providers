package com.yolt.providers.ing.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yolt.providers.common.pis.sepa.SepaLinksDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InitiatePaymentResponse {

    private String transactionStatus;
    private String paymentId;
    private List<TppMessages> tppMessages;

    @JsonProperty("_links")
    private SepaLinksDTO links;

}
