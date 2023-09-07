package com.yolt.providers.openbanking.ais.generic2.pec.submit.scheduled;

import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentExecutionHttpHeadersProvider;
import com.yolt.providers.openbanking.ais.generic2.pec.common.PaymentHttpHeadersFactory;
import com.yolt.providers.openbanking.ais.generic2.pec.submit.single.GenericSubmitPaymentPreExecutionResult;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticScheduled2;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;

@RequiredArgsConstructor
public class GenericSubmitScheduledPaymentHttpHeadersProvider implements PaymentExecutionHttpHeadersProvider<GenericSubmitPaymentPreExecutionResult, OBWriteDomesticScheduled2> {

    private final PaymentHttpHeadersFactory httpHeadersFactory;

    @Override
    public HttpHeaders provideHttpHeaders(GenericSubmitPaymentPreExecutionResult preExecutionResult, OBWriteDomesticScheduled2 httpRequestBody) {
        return httpHeadersFactory.createPaymentHttpHeaders(preExecutionResult.getAccessToken(),
                preExecutionResult.getAuthMeans(),
                preExecutionResult.getSigner(),
                httpRequestBody);
    }
}
