package com.yolt.providers.cbiglobe.common.pis.pec.initiate;

import com.yolt.providers.cbiglobe.common.model.InitiatePaymentRequest;
import com.yolt.providers.cbiglobe.common.pis.pec.CbiGlobePisHttpHeadersFactory;
import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentExecutionHttpHeadersProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;

import java.time.Clock;

@RequiredArgsConstructor
public class CbiGlobeInitiatePaymentHttpHeadersProvider implements PaymentExecutionHttpHeadersProvider<CbiGlobeSepaInitiatePreExecutionResult, InitiatePaymentRequest> {

    private final CbiGlobePisHttpHeadersFactory httpHeadersFactory;
    private final Clock clock;

    @Override
    public HttpHeaders provideHttpHeaders(CbiGlobeSepaInitiatePreExecutionResult preExecutionResult, InitiatePaymentRequest initiatePaymentRequest) {
        return httpHeadersFactory.createPaymentInitiationHttpHeaders(
                preExecutionResult.getAccessToken(),
                preExecutionResult.getAspspData(),
                preExecutionResult.getSignatureData(),
                preExecutionResult.getPsuIpAddress(),
                preExecutionResult.getRedirectUrlWithState(),
                initiatePaymentRequest,
                clock);
    }
}
