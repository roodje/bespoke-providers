package com.yolt.providers.bunq.common.pis.pec.status;

import com.bunq.sdk.security.SecurityUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.bunq.beanconfig.BunqDetailsProvider;
import com.yolt.providers.bunq.common.auth.BunqAuthenticationMeansV2;
import com.yolt.providers.bunq.common.http.BunqPisHttpClientFactory;
import com.yolt.providers.bunq.common.model.Psd2SessionResponse;
import com.yolt.providers.bunq.common.pis.pec.PaymentProviderState;
import com.yolt.providers.bunq.common.pis.pec.exception.PreExecutionMapperException;
import com.yolt.providers.bunq.common.pis.pec.session.Psd2SessionService;
import com.yolt.providers.bunq.common.pis.pec.submitandstatus.DefaultSubmitAndStatusPaymentPreExecutionResult;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.status.SepaStatusPaymentPreExecutionResultMapper;
import com.yolt.providers.common.pis.paymentexecutioncontext.exception.PaymentExecutionTechnicalException;
import com.yolt.providers.common.pis.sepa.GetStatusRequest;
import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

@RequiredArgsConstructor
public class DefaultStatusPaymentPreExecutionResultMapper implements SepaStatusPaymentPreExecutionResultMapper<DefaultSubmitAndStatusPaymentPreExecutionResult> {

    private final BunqPisHttpClientFactory httpClientFactory;
    private final Psd2SessionService sessionService;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    @Override
    public DefaultSubmitAndStatusPaymentPreExecutionResult map(final GetStatusRequest getStatusRequest) {
        var authMeans = BunqAuthenticationMeansV2.fromAuthenticationMeans(getStatusRequest.getAuthenticationMeans(), BunqDetailsProvider.BUNQ_PROVIDER_IDENTIFIER);
        var httpClient = httpClientFactory.createHttpClient(getStatusRequest.getRestTemplateManager(), BunqDetailsProvider.BUNQ_PROVIDER_DISPLAY_NAME);

        var providerState = deserializeProviderState(getStatusRequest.getProviderState());
        var keyPair = SecurityUtils.createKeyPairFromFormattedStrings(providerState.getKeyPairPublic(), providerState.getKeyPairPrivate());
        var sessionToken = providerState.getSessionToken();
        var expirationTime = providerState.getExpirationTime();
        if (Instant.ofEpochMilli(expirationTime).isBefore(Instant.now(clock))) {
            keyPair = SecurityUtils.generateKeyPair();
            Psd2SessionResponse psd2SessionResponse = null;
            try {
                psd2SessionResponse = sessionService.createSession(httpClient, keyPair, authMeans.getPsd2apiKey());
            } catch (JsonProcessingException | TokenInvalidException e) {
                throw new PreExecutionMapperException("Error occurred during preparing PreExecutionResult", e);
            }
            sessionToken = psd2SessionResponse.getToken().getTokenString();
            expirationTime = Instant.now(clock).plusSeconds(psd2SessionResponse.getExpiryTimeInSeconds()).toEpochMilli();
        }

        return new DefaultSubmitAndStatusPaymentPreExecutionResult(
                httpClient,
                providerState.getPaymentId(),
                authMeans.getPsd2UserId(),
                sessionToken,
                expirationTime,
                keyPair);
    }

    private PaymentProviderState deserializeProviderState(String providerState) {
        try {
            return objectMapper.readValue(Objects.requireNonNull(providerState), PaymentProviderState.class);
        } catch (JsonProcessingException e) {
            throw PaymentExecutionTechnicalException.paymentSubmissionException(e);
        }
    }
}
