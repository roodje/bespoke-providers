package com.yolt.providers.volksbank.common.pis.pec.submit;

import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentExecutionHttpHeadersProvider;
import com.yolt.providers.volksbank.common.pis.pec.VolksbankPisHttpHeadersFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;

@RequiredArgsConstructor
public class VolksbankSubmitPaymentHttpHeadersProvider implements PaymentExecutionHttpHeadersProvider<VolksbankSepaSubmitPreExecutionResult, Void> {

    private final VolksbankPisHttpHeadersFactory httpHeadersFactory;

    @Override
    public HttpHeaders provideHttpHeaders(VolksbankSepaSubmitPreExecutionResult preExecutionResult, Void unused) {
        return httpHeadersFactory.createCommonHttpHeaders(preExecutionResult.getAuthenticationMeans().getClientId());
    }
}
