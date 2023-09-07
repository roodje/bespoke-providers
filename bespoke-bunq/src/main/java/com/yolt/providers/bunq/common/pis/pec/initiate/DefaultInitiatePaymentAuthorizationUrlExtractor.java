package com.yolt.providers.bunq.common.pis.pec.initiate;

import com.yolt.providers.bunq.common.model.PaymentServiceProviderDraftPaymentResponse;
import com.yolt.providers.bunq.common.service.authorization.BunqAuthorizationServiceV5;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentAuthorizationUrlExtractor;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DefaultInitiatePaymentAuthorizationUrlExtractor implements PaymentAuthorizationUrlExtractor<PaymentServiceProviderDraftPaymentResponse, DefaultInitiatePaymentPreExecutionResult> {

    private final BunqAuthorizationServiceV5 authorizationServiceV5;

    @Override
    public String extractAuthorizationUrl(PaymentServiceProviderDraftPaymentResponse response, DefaultInitiatePaymentPreExecutionResult preExecutionResult) {
        return authorizationServiceV5.getLoginUrl(preExecutionResult.getClientId(),
                preExecutionResult.getRedirectUrl(),
                preExecutionResult.getState());
    }
}
