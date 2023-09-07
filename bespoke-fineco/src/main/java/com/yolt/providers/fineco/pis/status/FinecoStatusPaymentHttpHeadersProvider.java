package com.yolt.providers.fineco.pis.status;

import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentExecutionHttpHeadersProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@RequiredArgsConstructor
public class FinecoStatusPaymentHttpHeadersProvider implements PaymentExecutionHttpHeadersProvider<FinecoStatusPaymentPreExecutionResult, Void> {

    @Override
    public HttpHeaders provideHttpHeaders(FinecoStatusPaymentPreExecutionResult preExecutionResult, Void unused) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        return headers;
    }
}
