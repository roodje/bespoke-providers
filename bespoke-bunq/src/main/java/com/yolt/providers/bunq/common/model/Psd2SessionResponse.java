package com.yolt.providers.bunq.common.model;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

@ProjectedPayload
public interface Psd2SessionResponse {

    @JsonPath("$.Response.[?(@.Token)].Token")
    Token getToken();

    @JsonPath("$.Response.[?(@.UserPaymentServiceProvider)].UserPaymentServiceProvider.id")
    long getPsd2UserId();

    @JsonPath("$.Response.[?(@.UserPaymentServiceProvider)].UserPaymentServiceProvider.session_timeout")
    Long getExpiryTimeInSeconds();
}
