package com.yolt.providers.starlingbank.common.paymentexecutioncontext.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.pis.common.GetStatusRequest;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.uk.status.UkDomesticStatusPaymentPreExecutionResultMapper;
import com.yolt.providers.common.pis.paymentexecutioncontext.exception.PaymentExecutionTechnicalException;
import com.yolt.providers.common.pis.ukdomestic.UkProviderState;
import com.yolt.providers.starlingbank.common.auth.StarlingBankAuthMeansSupplier;
import com.yolt.providers.starlingbank.common.auth.StarlingBankAuthenticationMeans;
import com.yolt.providers.starlingbank.common.errorhandler.StepNotSupportedByBankException;
import com.yolt.providers.starlingbank.common.http.StarlingBankHttpClient;
import com.yolt.providers.starlingbank.common.http.StarlingBankHttpClientFactoryV4;
import com.yolt.providers.starlingbank.common.model.UkDomesticPaymentProviderState;
import com.yolt.providers.starlingbank.common.model.domain.Token;
import com.yolt.providers.starlingbank.common.paymentexecutioncontext.MalformedProviderStateException;
import com.yolt.providers.starlingbank.common.paymentexecutioncontext.StarlingBankHttpHeadersProducerFactory;
import com.yolt.providers.starlingbank.common.paymentexecutioncontext.model.StarlingBankSubmitAndStatusPaymentExecutionContextPreExecutionResult;
import com.yolt.providers.starlingbank.common.service.authorization.StarlingBankAuthorizationService;
import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;

@RequiredArgsConstructor
public class StarlingBankStatusPaymentPreExecutorResultMapper implements UkDomesticStatusPaymentPreExecutionResultMapper<StarlingBankSubmitAndStatusPaymentExecutionContextPreExecutionResult> {

    private static final String PAYMENTS_STATUS_URL_TEMPLATE = "/api/v2/payments/local/payment-order/%s/payments";

    private final StarlingBankAuthorizationService authorizationService;
    private final ObjectMapper objectMapper;
    private final StarlingBankHttpHeadersProducerFactory httpHeadersProducerFactory;
    private final StarlingBankHttpClientFactoryV4 httpClientFactory;
    private final StarlingBankAuthMeansSupplier authMeansSupplier;
    private final String providerIdentifierDisplayName;
    private final String providerIdentifier;
    private final Clock clock;

    @Override
    public StarlingBankSubmitAndStatusPaymentExecutionContextPreExecutionResult map(GetStatusRequest paymentRequest) throws PaymentExecutionTechnicalException {
        StarlingBankAuthenticationMeans authMeans = authMeansSupplier.createAuthenticationMeans(paymentRequest.getAuthenticationMeans(), providerIdentifier);
        StarlingBankHttpClient httpClient = httpClientFactory.createHttpClient(
                paymentRequest.getRestTemplateManager(),
                providerIdentifierDisplayName,
                httpHeadersProducerFactory.createHeadersProducer(authMeans, paymentRequest.getSigner()),
                authMeans);

        UkProviderState state;
        UkDomesticPaymentProviderState providerState;
        try {
            state = objectMapper.readValue(paymentRequest.getProviderState(), UkProviderState.class);
            providerState = objectMapper.convertValue(state.getOpenBankingPayment(), UkDomesticPaymentProviderState.class);
        } catch (JsonProcessingException | IllegalArgumentException e) {
            throw new MalformedProviderStateException("Unable to parse provider state for Status" );
        }


        if (providerState.isPaymentNotSubmittedYet()) {
            throw new StepNotSupportedByBankException("Getting status before submit is not supported by bank");
        }
        var preExecutionResult = StarlingBankSubmitAndStatusPaymentExecutionContextPreExecutionResult.builder()
                .expiresIn(providerState.getAccessTokenExpiresIn())
                .refreshToken(providerState.getRefreshToken())
                .token(providerState.getAccessToken())
                .externalPaymentId(providerState.getExternalPaymentId())
                .signer(paymentRequest.getSigner())
                .paymentRequest(providerState.getPaymentRequest())
                .httpClient(httpClient)
                .url(String.format(PAYMENTS_STATUS_URL_TEMPLATE, providerState.getExternalPaymentId()))
                .authenticationMeans(authMeans);

        if (providerState.getAccessTokenExpiresIn().before(Date.from(Instant.now(clock)))) {
            try {
                Token oAuthToken = authorizationService.getOAuthRefreshToken(httpClient, providerState.getRefreshToken(), authMeans);
                preExecutionResult
                        .token(oAuthToken.getAccessToken())
                        .refreshToken(oAuthToken.getRefreshToken())
                        .expiresIn(Date.from(Instant.now(clock).plusSeconds(oAuthToken.getExpiresIn())));
            } catch (TokenInvalidException e) {
                throw new GetAccessTokenFailedException(e);
            }
        }
        return preExecutionResult.build();
    }

    private <T> T readObject(String providerState, Class<T> responseClass) {
        try {
            return objectMapper.readValue(Objects.requireNonNull(providerState), responseClass);
        } catch (JsonProcessingException e) {
            throw PaymentExecutionTechnicalException.statusFailed(e);
        }
    }
}
