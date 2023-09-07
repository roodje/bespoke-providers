package com.yolt.providers.abnamrogroup.common.pis.pec.submit;

import com.yolt.providers.abnamrogroup.common.pis.pec.AbnAmroPaymentCommonHttpHeadersProvider;
import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentExecutionHttpHeadersProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;

@RequiredArgsConstructor
public class AbnAmroSubmitPaymentHttpHeadersProvider implements PaymentExecutionHttpHeadersProvider<AbnAmroSubmitPaymentPreExecutionResult, Void> {

    private final AbnAmroPaymentCommonHttpHeadersProvider commonHttpHeadersProvider;

    @Override
    public HttpHeaders provideHttpHeaders(AbnAmroSubmitPaymentPreExecutionResult preExecutionResult, Void unused) {
        return commonHttpHeadersProvider.provideCommonHttpHeaders(preExecutionResult.getAccessTokenResponseDTO().getAccessToken(), preExecutionResult.getAuthenticationMeans().getApiKey());
    }
}
