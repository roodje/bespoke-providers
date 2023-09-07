package com.yolt.providers.bunq.common.pis.pec.initiate;

import com.bunq.sdk.security.SecurityUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.yolt.providers.bunq.beanconfig.BunqDetailsProvider;
import com.yolt.providers.bunq.common.auth.BunqAuthenticationMeansV2;
import com.yolt.providers.bunq.common.http.BunqPisHttpClientFactory;
import com.yolt.providers.bunq.common.model.Psd2SessionResponse;
import com.yolt.providers.bunq.common.pis.pec.exception.PreExecutionMapperException;
import com.yolt.providers.bunq.common.pis.pec.session.Psd2SessionService;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.initiate.SepaInitiateSinglePaymentPreExecutionResultMapper;
import com.yolt.providers.common.pis.sepa.InitiatePaymentRequest;
import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.time.Instant;

@RequiredArgsConstructor
public class DefaultInitiatePaymentPreExecutionResultMapper implements SepaInitiateSinglePaymentPreExecutionResultMapper<DefaultInitiatePaymentPreExecutionResult> {

    private final BunqPisHttpClientFactory httpClientFactory;
    private final Psd2SessionService sessionService;
    private final Clock clock;

    @Override
    public DefaultInitiatePaymentPreExecutionResult map(InitiatePaymentRequest initiatePaymentRequest) {
        var authMeans = BunqAuthenticationMeansV2.fromAuthenticationMeans(initiatePaymentRequest.getAuthenticationMeans(), BunqDetailsProvider.BUNQ_PROVIDER_IDENTIFIER);
        var httpClient = httpClientFactory.createHttpClient(initiatePaymentRequest.getRestTemplateManager(), BunqDetailsProvider.BUNQ_PROVIDER_DISPLAY_NAME);
        var request = initiatePaymentRequest.getRequestDTO();

        var keyPair = SecurityUtils.generateKeyPair();
        Psd2SessionResponse psd2SessionResponse;
        try {
            psd2SessionResponse = sessionService.createSession(httpClient, keyPair, authMeans.getPsd2apiKey());
        } catch (JsonProcessingException | TokenInvalidException e) {
            throw new PreExecutionMapperException("Error occurred during preparing PreExecutionResult", e);
        }
        return new DefaultInitiatePaymentPreExecutionResult(
                httpClient,
                request,
                initiatePaymentRequest.getBaseClientRedirectUrl(),
                initiatePaymentRequest.getState(),
                authMeans.getClientId(),
                authMeans.getPsd2UserId(),
                psd2SessionResponse.getToken().getTokenString(),
                Instant.now(clock).plusSeconds(psd2SessionResponse.getExpiryTimeInSeconds()).toEpochMilli(),
                keyPair);
    }
}
