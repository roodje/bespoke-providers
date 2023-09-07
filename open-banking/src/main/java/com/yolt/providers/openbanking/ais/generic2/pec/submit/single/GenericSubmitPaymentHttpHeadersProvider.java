package com.yolt.providers.openbanking.ais.generic2.pec.submit.single;

import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentExecutionHttpHeadersProvider;
import com.yolt.providers.openbanking.ais.generic2.pec.common.PaymentHttpHeadersFactory;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomestic2;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;

@RequiredArgsConstructor
public class GenericSubmitPaymentHttpHeadersProvider implements PaymentExecutionHttpHeadersProvider<GenericSubmitPaymentPreExecutionResult, OBWriteDomestic2> {

    private final PaymentHttpHeadersFactory httpHeadersFactory;

    @Override
    public HttpHeaders provideHttpHeaders(GenericSubmitPaymentPreExecutionResult preExecutionResult, OBWriteDomestic2 httpRequestBody) {
        return httpHeadersFactory.createPaymentHttpHeaders(preExecutionResult.getAccessToken(),
                preExecutionResult.getAuthMeans(),
                preExecutionResult.getSigner(),
                httpRequestBody);
    }
}
