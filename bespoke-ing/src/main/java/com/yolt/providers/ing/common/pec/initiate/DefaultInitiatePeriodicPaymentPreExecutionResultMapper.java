package com.yolt.providers.ing.common.pec.initiate;

import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.initiate.SepaInitiatePeriodicPaymentPreExecutionResultMapper;
import com.yolt.providers.common.pis.sepa.InitiatePaymentRequest;
import com.yolt.providers.ing.common.auth.IngAuthenticationMeans;
import com.yolt.providers.ing.common.auth.IngClientAccessMeans;
import com.yolt.providers.ing.common.pec.DefaultPisAccessMeansProvider;
import lombok.RequiredArgsConstructor;

import java.time.Clock;

@RequiredArgsConstructor
public class DefaultInitiatePeriodicPaymentPreExecutionResultMapper implements SepaInitiatePeriodicPaymentPreExecutionResultMapper<DefaultInitiatePaymentPreExecutionResult> {

    private final DefaultPisAccessMeansProvider accessTokenProvider;
    private final String providerIdentifier;
    private final Clock clock;

    @Override
    public DefaultInitiatePaymentPreExecutionResult map(final InitiatePaymentRequest initiatePaymentRequest) {
        IngAuthenticationMeans authenticationMeans = IngAuthenticationMeans.createIngAuthenticationMeans(initiatePaymentRequest.getAuthenticationMeans(), providerIdentifier);
        IngClientAccessMeans clientAccessMeans = accessTokenProvider.getClientAccessMeans(
                authenticationMeans,
                initiatePaymentRequest.getRestTemplateManager(),
                initiatePaymentRequest.getSigner(),
                clock
        );

        return new DefaultInitiatePaymentPreExecutionResult(
                initiatePaymentRequest.getRequestDTO(),
                initiatePaymentRequest.getRestTemplateManager(),
                authenticationMeans,
                clientAccessMeans,
                initiatePaymentRequest.getSigner(),
                initiatePaymentRequest.getBaseClientRedirectUrl(),
                initiatePaymentRequest.getState(),
                initiatePaymentRequest.getPsuIpAddress()
        );
    }
}
