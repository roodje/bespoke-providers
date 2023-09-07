package com.yolt.providers.knabgroup.common.payment.initiate;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.initiate.SepaInitiateSinglePaymentPreExecutionResultMapper;
import com.yolt.providers.common.pis.sepa.InitiatePaymentRequest;
import com.yolt.providers.knabgroup.common.auth.KnabGroupAuthenticationMeans;
import com.yolt.providers.knabgroup.common.payment.DefaultPisAccessTokenProvider;
import com.yolt.providers.knabgroup.common.payment.dto.Internal.InitiatePaymentPreExecutionResult;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class DefaultInitiatePaymentPreExecutionResultMapper implements SepaInitiateSinglePaymentPreExecutionResultMapper<InitiatePaymentPreExecutionResult> {
    private final String providerIdentifier;
    private final DefaultPisAccessTokenProvider accessMeansProvider;

    @Override
    public InitiatePaymentPreExecutionResult map(InitiatePaymentRequest request) {
        KnabGroupAuthenticationMeans authenticationMeans = KnabGroupAuthenticationMeans.createKnabGroupAuthenticationMeans(request.getAuthenticationMeans(), providerIdentifier);
        RestTemplateManager restTemplateManager = request.getRestTemplateManager();
        return new InitiatePaymentPreExecutionResult(request.getRequestDTO(),
                restTemplateManager,
                authenticationMeans,
                accessMeansProvider.getClientAccessToken(authenticationMeans, restTemplateManager),
                request.getSigner(),
                request.getBaseClientRedirectUrl(),
                request.getState(),
                request.getPsuIpAddress());
    }
}
