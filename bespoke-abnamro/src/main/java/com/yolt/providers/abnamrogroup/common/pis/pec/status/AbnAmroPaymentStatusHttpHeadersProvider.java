package com.yolt.providers.abnamrogroup.common.pis.pec.status;

import com.yolt.providers.abnamrogroup.common.pis.pec.AbnAmroPaymentCommonHttpHeadersProvider;
import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentExecutionHttpHeadersProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;

@RequiredArgsConstructor
public class AbnAmroPaymentStatusHttpHeadersProvider implements PaymentExecutionHttpHeadersProvider<AbnAmroPaymentStatusPreExecutionResult, Void> {

    private final AbnAmroPaymentCommonHttpHeadersProvider commonHttpHeadersProvider;

    @Override
    public HttpHeaders provideHttpHeaders(AbnAmroPaymentStatusPreExecutionResult preExecutionResult, Void unused) {
        return commonHttpHeadersProvider.provideCommonHttpHeaders(preExecutionResult.getProviderState().getUserAccessTokenState().getAccessToken(),
                preExecutionResult.getAuthenticationMeans().getApiKey());
    }
}
