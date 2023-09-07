package com.yolt.providers.rabobank.pis.pec.initiate;

import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.initiate.SepaInitiatePaymentPreExecutionResultMapper;
import com.yolt.providers.common.pis.sepa.InitiatePaymentRequest;
import com.yolt.providers.rabobank.RabobankAuthenticationMeans;

import static com.yolt.providers.rabobank.RabobankAuthenticationMeans.fromPISAuthenticationMeans;

public class RabobankSepaInitiatePreExecutionResultMapper implements SepaInitiatePaymentPreExecutionResultMapper<RabobankSepaInitiatePreExecutionResult> {

    @Override
    public RabobankSepaInitiatePreExecutionResult map(InitiatePaymentRequest initiatePaymentRequest) {
        RabobankAuthenticationMeans authenticationMeans = fromPISAuthenticationMeans(initiatePaymentRequest.getAuthenticationMeans());
        return new RabobankSepaInitiatePreExecutionResult(authenticationMeans,
                initiatePaymentRequest.getRequestDTO(),
                initiatePaymentRequest.getBaseClientRedirectUrl(),
                initiatePaymentRequest.getSigner(),
                initiatePaymentRequest.getPsuIpAddress(),
                initiatePaymentRequest.getRestTemplateManager(),
                initiatePaymentRequest.getState());
    }
}
