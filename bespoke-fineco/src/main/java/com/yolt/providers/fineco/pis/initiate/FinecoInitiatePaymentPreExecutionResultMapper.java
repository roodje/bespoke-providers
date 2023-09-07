package com.yolt.providers.fineco.pis.initiate;

import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.initiate.SepaInitiateSinglePaymentPreExecutionResultMapper;
import com.yolt.providers.common.pis.sepa.InitiatePaymentRequest;
import com.yolt.providers.fineco.auth.FinecoAuthenticationMeans;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FinecoInitiatePaymentPreExecutionResultMapper implements SepaInitiateSinglePaymentPreExecutionResultMapper<FinecoInitiatePaymentPreExecutionResult> {

    private final String providerIdentifier;

    @Override
    public FinecoInitiatePaymentPreExecutionResult map(InitiatePaymentRequest initiatePaymentRequest) {
        FinecoAuthenticationMeans authenticationMeans = FinecoAuthenticationMeans.fromAuthenticationMeans(initiatePaymentRequest.getAuthenticationMeans(), providerIdentifier);

        return new FinecoInitiatePaymentPreExecutionResult(
                initiatePaymentRequest.getRequestDTO(),
                initiatePaymentRequest.getRestTemplateManager(),
                authenticationMeans,
                initiatePaymentRequest.getSigner(),
                initiatePaymentRequest.getBaseClientRedirectUrl(),
                initiatePaymentRequest.getState(),
                initiatePaymentRequest.getPsuIpAddress()
        );
    }
}
