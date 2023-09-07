package com.yolt.providers.yoltprovider.pis.sepa.pecadapter.initiate;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.yolt.providers.common.pis.sepa.SepaPaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@Getter
@AllArgsConstructor
public class SepaInitiatePaymentResponse {

    private String scaRedirect;
    private String paymentId;
    private SepaPaymentStatus status;

    @JsonProperty("_links")
    private void unpackScaRedirectFromNested(Map<String, String> links) {
        scaRedirect = links.get("scaRedirect");
    }

    @JsonProperty("paymentStatus")
    private void unpackPaymentStatus(Map<String, String> paymentStatus) {
        this.paymentId = paymentStatus.get("paymentId");
        this.status = SepaPaymentStatus.valueOf(paymentStatus.get("paymentStatus"));
    }
}