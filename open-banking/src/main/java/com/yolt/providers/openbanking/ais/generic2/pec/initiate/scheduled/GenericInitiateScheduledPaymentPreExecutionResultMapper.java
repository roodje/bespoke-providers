package com.yolt.providers.openbanking.ais.generic2.pec.initiate.scheduled;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.CreationFailedException;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.uk.initiate.UkInitiateScheduledPaymentPreExecutionResultMapper;
import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticScheduledPaymentRequest;
import com.yolt.providers.openbanking.ais.common.UkDomesticScheduledPaymentValidator;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.pec.auth.GenericPaymentAccessTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.util.Map;
import java.util.function.Function;

@RequiredArgsConstructor

public class GenericInitiateScheduledPaymentPreExecutionResultMapper implements UkInitiateScheduledPaymentPreExecutionResultMapper<GenericInitiateScheduledPaymentPreExecutionResult> {

    private static final String INVALID_PAYMENT_ERROR_MESSAGE = "Payment is not valid! This should've been caught earlier in the process.";

    private final Function<Map<String, BasicAuthenticationMean>, DefaultAuthMeans> getAuthenticationMeans;
    private final GenericPaymentAccessTokenProvider paymentAccessTokenProvider;

    @SneakyThrows
    @Override
    public GenericInitiateScheduledPaymentPreExecutionResult map(InitiateUkDomesticScheduledPaymentRequest initiateUkDomesticPaymentRequest) {
        if (!UkDomesticScheduledPaymentValidator.isValid(initiateUkDomesticPaymentRequest)) {
            throw new CreationFailedException(INVALID_PAYMENT_ERROR_MESSAGE);
        }

        var restTemplateManager = initiateUkDomesticPaymentRequest.getRestTemplateManager();
        var authMeans = getAuthenticationMeans.apply(initiateUkDomesticPaymentRequest.getAuthenticationMeans());
        var authenticationMeansReference = initiateUkDomesticPaymentRequest.getAuthenticationMeansReference();
        var signer = initiateUkDomesticPaymentRequest.getSigner();

        var accessMeans = paymentAccessTokenProvider.provideClientAccessToken(restTemplateManager, authMeans, authenticationMeansReference, signer);

        var preExecutionResult = new GenericInitiateScheduledPaymentPreExecutionResult();
        preExecutionResult.setAuthMeans(authMeans);
        preExecutionResult.setBaseClientRedirectUrl(initiateUkDomesticPaymentRequest.getBaseClientRedirectUrl());
        preExecutionResult.setSigner(signer);
        preExecutionResult.setState(initiateUkDomesticPaymentRequest.getState());
        preExecutionResult.setRestTemplateManager(restTemplateManager);
        preExecutionResult.setPaymentRequestDTO(initiateUkDomesticPaymentRequest.getRequestDTO());
        preExecutionResult.setAccessToken(accessMeans.getAccessToken());
        preExecutionResult.setAuthenticationMeansReference(authenticationMeansReference);
        return preExecutionResult;
    }
}
