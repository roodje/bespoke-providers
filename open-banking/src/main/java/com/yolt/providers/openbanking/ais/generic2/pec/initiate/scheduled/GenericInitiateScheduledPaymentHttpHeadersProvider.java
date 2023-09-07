package com.yolt.providers.openbanking.ais.generic2.pec.initiate.scheduled;

import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentExecutionHttpHeadersProvider;
import com.yolt.providers.openbanking.ais.generic2.pec.common.PaymentHttpHeadersFactory;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticScheduledConsent4;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;

@RequiredArgsConstructor
public class GenericInitiateScheduledPaymentHttpHeadersProvider implements PaymentExecutionHttpHeadersProvider<GenericInitiateScheduledPaymentPreExecutionResult, OBWriteDomesticScheduledConsent4> {

    private final PaymentHttpHeadersFactory httpHeadersFactory;

    @Override
    public HttpHeaders provideHttpHeaders(GenericInitiateScheduledPaymentPreExecutionResult preExecutionResult, OBWriteDomesticScheduledConsent4 httpRequestBody) {
        return httpHeadersFactory.createPaymentHttpHeaders(preExecutionResult.getAccessToken(),
                preExecutionResult.getAuthMeans(),
                preExecutionResult.getSigner(),
                httpRequestBody);
    }
}
