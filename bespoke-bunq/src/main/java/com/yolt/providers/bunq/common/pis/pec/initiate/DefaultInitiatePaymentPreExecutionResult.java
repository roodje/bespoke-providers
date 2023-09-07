package com.yolt.providers.bunq.common.pis.pec.initiate;

import com.yolt.providers.bunq.common.http.BunqPisHttpClient;
import com.yolt.providers.common.pis.sepa.SepaInitiatePaymentRequestDTO;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.security.KeyPair;

@RequiredArgsConstructor
@Data
public class DefaultInitiatePaymentPreExecutionResult {

    private final BunqPisHttpClient httpClient;
    private final SepaInitiatePaymentRequestDTO request;
    private final String redirectUrl;
    private final String state;
    private final String clientId;
    private final long psd2UserId;
    private final String sessionToken;
    private final long expirationTime;
    private final KeyPair keyPair;
}
