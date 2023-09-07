package com.yolt.providers.openbanking.ais.generic2.pec.initiate.single;

import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentExecutionHttpHeadersProvider;
import com.yolt.providers.openbanking.ais.generic2.pec.common.PaymentHttpHeadersFactory;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticConsent4;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;

@RequiredArgsConstructor
public class GenericInitiatePaymentHttpHeadersProvider implements PaymentExecutionHttpHeadersProvider<GenericInitiatePaymentPreExecutionResult, OBWriteDomesticConsent4> {

    private final PaymentHttpHeadersFactory httpHeadersFactory;

    @Override
    public HttpHeaders provideHttpHeaders(GenericInitiatePaymentPreExecutionResult preExecutionResult, OBWriteDomesticConsent4 httpRequestBody) {
        return httpHeadersFactory.createPaymentHttpHeaders(preExecutionResult.getAccessToken(),
                preExecutionResult.getAuthMeans(),
                preExecutionResult.getSigner(),
                httpRequestBody);
    }
}
