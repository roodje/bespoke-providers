package com.yolt.providers.openbanking.ais.generic2.pec.submit.single;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.ConfirmationFailedException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.pis.common.SubmitPaymentRequest;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.uk.submit.UkDomesticSubmitPreExecutionResultMapper;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.generic2.pec.auth.GenericPaymentAccessTokenProvider;
import com.yolt.providers.openbanking.ais.generic2.pec.common.UkProviderStateDeserializer;
import com.yolt.providers.openbanking.ais.generic2.pec.common.exception.GenericPaymentRequestInvocationException;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.function.Function;

@RequiredArgsConstructor
public class GenericSubmitPreExecutionResultMapper implements UkDomesticSubmitPreExecutionResultMapper<GenericSubmitPaymentPreExecutionResult> {

    private final Function<Map<String, BasicAuthenticationMean>, DefaultAuthMeans> getAuthenticationMeans;
    private final GenericPaymentAccessTokenProvider paymentAccessTokenProvider;
    private final UkProviderStateDeserializer ukProviderStateDeserializer;

    @Override
    public GenericSubmitPaymentPreExecutionResult map(SubmitPaymentRequest submitPaymentRequest) {
        var ukProviderState = ukProviderStateDeserializer.deserialize(submitPaymentRequest.getProviderState());
        var authMeans = getAuthenticationMeans.apply(submitPaymentRequest.getAuthenticationMeans());
        var restTemplateManager = submitPaymentRequest.getRestTemplateManager();
        var signer = submitPaymentRequest.getSigner();
        AccessMeans accessMeans = getUserAccessMeans(restTemplateManager, authMeans, signer, submitPaymentRequest.getRedirectUrlPostedBackFromSite());

        var preExecutionResult = new GenericSubmitPaymentPreExecutionResult();
        preExecutionResult.setAuthMeans(authMeans);
        preExecutionResult.setRestTemplateManager(restTemplateManager);
        preExecutionResult.setSigner(signer);
        preExecutionResult.setAccessToken(accessMeans.getAccessToken());
        preExecutionResult.setProviderState(ukProviderState);
        return preExecutionResult;
    }

    private AccessMeans getUserAccessMeans(RestTemplateManager restTemplateManager,
                                           DefaultAuthMeans authMeans,
                                           Signer signer,
                                           String redirectUrlPostedBackFromSite) {
        try {
            return paymentAccessTokenProvider.provideUserAccessToken(restTemplateManager, authMeans, redirectUrlPostedBackFromSite, signer, null);
        } catch (TokenInvalidException | ConfirmationFailedException e) {
            throw new GenericPaymentRequestInvocationException(e);
        }

    }
}
