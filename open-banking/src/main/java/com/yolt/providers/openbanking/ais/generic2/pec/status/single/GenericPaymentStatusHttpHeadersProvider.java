package com.yolt.providers.openbanking.ais.generic2.pec.status.single;

import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentExecutionHttpHeadersProvider;
import com.yolt.providers.openbanking.ais.generic2.pec.common.PaymentHttpHeadersFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;

@RequiredArgsConstructor
public class GenericPaymentStatusHttpHeadersProvider implements PaymentExecutionHttpHeadersProvider<GenericPaymentStatusPreExecutionResult, Void> {

    private final PaymentHttpHeadersFactory httpHeadersFactory;

    @Override
    public HttpHeaders provideHttpHeaders(GenericPaymentStatusPreExecutionResult preExecutionResult, Void unused) {
        return httpHeadersFactory.createCommonPaymentHttpHeaders(preExecutionResult.getAccessToken(), preExecutionResult.getAuthMeans());
    }
}
