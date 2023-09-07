package com.yolt.providers.ing.common.pec.submit;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.pis.common.PaymentType;
import com.yolt.providers.ing.common.auth.IngAuthenticationMeans;
import com.yolt.providers.ing.common.auth.IngClientAccessMeans;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@Value
@RequiredArgsConstructor
public class DefaultSubmitPaymentPreExecutionResult {

    private final String paymentId;
    private final RestTemplateManager restTemplateManager;
    private final IngAuthenticationMeans authenticationMeans;
    private final IngClientAccessMeans clientAccessMeans;
    private final Signer signer;
    private final String psuIpAddress;
    private final PaymentType paymentType;
}
