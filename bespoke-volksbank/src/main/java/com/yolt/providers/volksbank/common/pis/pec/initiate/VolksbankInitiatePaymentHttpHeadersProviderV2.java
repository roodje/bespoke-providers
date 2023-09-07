package com.yolt.providers.volksbank.common.pis.pec.initiate;

import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentExecutionHttpHeadersProvider;
import com.yolt.providers.volksbank.common.pis.pec.VolksbankPisHttpHeadersFactory;
import com.yolt.providers.volksbank.dto.v1_1.InitiatePaymentRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;

@RequiredArgsConstructor
public class VolksbankInitiatePaymentHttpHeadersProviderV2 implements PaymentExecutionHttpHeadersProvider<VolksbankSepaInitiatePreExecutionResult, InitiatePaymentRequest> {

    private final VolksbankPisHttpHeadersFactory httpHeadersFactory;

    @Override
    public HttpHeaders provideHttpHeaders(VolksbankSepaInitiatePreExecutionResult preExecutionResult, InitiatePaymentRequest initiatePaymentRequest) {
        return httpHeadersFactory.createPaymentInitiationHttpHeaders(preExecutionResult.getAuthenticationMeans().getClientId(), preExecutionResult.getPsuIpAddress());
    }
}
