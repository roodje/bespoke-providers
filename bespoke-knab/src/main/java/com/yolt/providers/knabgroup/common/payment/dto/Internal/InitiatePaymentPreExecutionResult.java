package com.yolt.providers.knabgroup.common.payment.dto.Internal;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.pis.sepa.SepaInitiatePaymentRequestDTO;
import com.yolt.providers.knabgroup.common.auth.KnabGroupAuthenticationMeans;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class InitiatePaymentPreExecutionResult {

    private final SepaInitiatePaymentRequestDTO requestDTO;
    private final RestTemplateManager restTemplateManager;
    private final KnabGroupAuthenticationMeans authenticationMeans;
    private final String accessToken;
    private final Signer signer;
    private final String baseClientRedirectUrl;
    private final String state;
    private final String psuIpAddress;
}
