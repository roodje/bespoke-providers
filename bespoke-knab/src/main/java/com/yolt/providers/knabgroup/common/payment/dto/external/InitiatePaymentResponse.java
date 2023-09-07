package com.yolt.providers.knabgroup.common.payment.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@Getter
@AllArgsConstructor
public class InitiatePaymentResponse {

    private String paymentId;

    private String transactionStatus;

    private String redirectUrl;

    @JsonProperty("_links")
    private void unpackNameFromNestedObject(Map<String, Map<String, String>> links) {
        if (links != null && links.get("scaRedirect") != null) {
            redirectUrl = links.get("scaRedirect").get("href");
        }
    }
}
