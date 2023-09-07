package com.yolt.providers.cbiglobe.common.pis.pec.submit;

import com.yolt.providers.cbiglobe.common.pis.pec.CbiGlobePisHttpHeadersFactory;
import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentExecutionHttpHeadersProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;

import java.time.Clock;

@RequiredArgsConstructor
public class CbiGlobeSubmitPaymentHttpHeadersProvider implements PaymentExecutionHttpHeadersProvider<CbiGlobeSepaSubmitPreExecutionResult, Void> {

    private final CbiGlobePisHttpHeadersFactory httpHeadersFactory;
    private final Clock clock;

    @Override
    public HttpHeaders provideHttpHeaders(CbiGlobeSepaSubmitPreExecutionResult preExecutionResult, Void unused) {
        return httpHeadersFactory.createPaymentStatusHeaders(
                preExecutionResult.getAccessToken(),
                preExecutionResult.getAspspData(),
                preExecutionResult.getSignatureData(),
                clock);
    }
}
