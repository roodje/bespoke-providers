package com.yolt.providers.starlingbank.common.paymentexecutioncontext.extractor;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.pis.paymentexecutioncontext.exception.PaymentExecutionTechnicalException;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentAuthorizationUrlExtractor;
import com.yolt.providers.starlingbank.common.auth.StarlingBankAuthMeansSupplier;
import com.yolt.providers.starlingbank.common.auth.StarlingBankAuthenticationMeans;
import com.yolt.providers.starlingbank.common.paymentexecutioncontext.model.StarlingBankInitiatePaymentExecutionContextPreExecutionResult;
import com.yolt.providers.starlingbank.common.service.authorization.StarlingBankAuthorizationService;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class StarlingBankPaymentAuthorizationUrlExtractor implements PaymentAuthorizationUrlExtractor<String, StarlingBankInitiatePaymentExecutionContextPreExecutionResult> {

    private final StarlingBankAuthorizationService authorizationService;
    private final StarlingBankAuthMeansSupplier authMeansSupplier;
    private final String providerIdentifier;

    @Override
    public String extractAuthorizationUrl(String s, StarlingBankInitiatePaymentExecutionContextPreExecutionResult preExecutionResult) throws PaymentExecutionTechnicalException {
        StarlingBankAuthenticationMeans authMeans = createAuthMeans(preExecutionResult.getBasicAuthMeans());
        return authorizationService.getLoginUrl(
                preExecutionResult.getBaseRedirectUrl(),
                preExecutionResult.getState(),
                authMeans);
    }

    private StarlingBankAuthenticationMeans createAuthMeans(Map<String, BasicAuthenticationMean> basicAuthMeans) {
        return authMeansSupplier.createAuthenticationMeans(basicAuthMeans, providerIdentifier);
    }
}
