package com.yolt.providers.cbiglobe.common.model;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

@ProjectedPayload
public interface PaymentResponse {

    @JsonPath("$.transactionStatus")
    Status getTransactionStatus();

    @JsonPath("$.paymentId")
    String getPaymentId();

    @JsonPath("$._links.updatePsuAuthenticationRedirect.href")
    String getRedirectUrl();

    enum Status {
        ACCP,
        ACSC,
        ACSP,
        ACTC,
        ACWC,
        ACWP,
        RCVD,
        PDNG,
        RJCT
    }
}
