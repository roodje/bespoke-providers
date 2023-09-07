package com.yolt.providers.ing.common.pec.initiate;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.pis.sepa.SepaInitiatePaymentRequestDTO;
import com.yolt.providers.ing.common.auth.IngAuthenticationMeans;
import com.yolt.providers.ing.common.auth.IngClientAccessMeans;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@Value
@RequiredArgsConstructor
public class DefaultInitiatePaymentPreExecutionResult {

    private final SepaInitiatePaymentRequestDTO requestDTO;
    private final RestTemplateManager restTemplateManager;
    private final IngAuthenticationMeans authenticationMeans;
    private final IngClientAccessMeans clientAccessMeans;
    private final Signer signer;
    private final String baseClientRedirectUrl;
    private final String state;
    private final String psuIpAddress;
}
