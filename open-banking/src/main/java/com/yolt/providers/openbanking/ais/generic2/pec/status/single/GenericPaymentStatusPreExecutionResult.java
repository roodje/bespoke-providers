package com.yolt.providers.openbanking.ais.generic2.pec.status.single;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import lombok.Value;

@Value
public class GenericPaymentStatusPreExecutionResult {

    private String accessToken;
    private DefaultAuthMeans authMeans;
    private RestTemplateManager restTemplateManager;
    private String paymentId;
    private String consentId;
}
