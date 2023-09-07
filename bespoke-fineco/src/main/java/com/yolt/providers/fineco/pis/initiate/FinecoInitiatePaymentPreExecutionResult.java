package com.yolt.providers.fineco.pis.initiate;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.pis.sepa.SepaInitiatePaymentRequestDTO;
import com.yolt.providers.fineco.auth.FinecoAuthenticationMeans;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@Value
@RequiredArgsConstructor
public class FinecoInitiatePaymentPreExecutionResult {

    private final SepaInitiatePaymentRequestDTO requestDTO;
    private final RestTemplateManager restTemplateManager;
    private final FinecoAuthenticationMeans authenticationMeans;
    private final Signer signer;
    private final String baseClientRedirectUrl;
    private final String state;
    private final String psuIpAddress;
}
