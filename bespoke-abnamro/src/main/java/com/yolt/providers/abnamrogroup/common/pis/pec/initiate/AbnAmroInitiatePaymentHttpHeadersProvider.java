package com.yolt.providers.abnamrogroup.common.pis.pec.initiate;

import com.yolt.providers.abnamro.pis.SepaPayment;
import com.yolt.providers.abnamrogroup.common.pis.pec.AbnAmroPaymentCommonHttpHeadersProvider;
import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentExecutionHttpHeadersProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;

@RequiredArgsConstructor
public class AbnAmroInitiatePaymentHttpHeadersProvider implements PaymentExecutionHttpHeadersProvider<AbnAmroInitiatePaymentPreExecutionResult, SepaPayment> {

    private final AbnAmroPaymentCommonHttpHeadersProvider commonHttpHeadersProvider;

    @Override
    public HttpHeaders provideHttpHeaders(AbnAmroInitiatePaymentPreExecutionResult preExecutionResult, SepaPayment sepaPayment) {
        return commonHttpHeadersProvider.provideCommonHttpHeaders(preExecutionResult.getAccessToken(), preExecutionResult.getAuthenticationMeans().getApiKey());
    }
}
