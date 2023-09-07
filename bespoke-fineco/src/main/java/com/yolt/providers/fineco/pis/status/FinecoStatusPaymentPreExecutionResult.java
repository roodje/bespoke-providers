package com.yolt.providers.fineco.pis.status;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.pis.common.PaymentType;
import com.yolt.providers.fineco.auth.FinecoAuthenticationMeans;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@Value
@RequiredArgsConstructor
public class FinecoStatusPaymentPreExecutionResult {

    private final String paymentId;
    private final RestTemplateManager restTemplateManager;
    private final FinecoAuthenticationMeans authenticationMeans;
    private final Signer signer;
    private final String psuIpAddress;
    private final PaymentType paymentType;
}
