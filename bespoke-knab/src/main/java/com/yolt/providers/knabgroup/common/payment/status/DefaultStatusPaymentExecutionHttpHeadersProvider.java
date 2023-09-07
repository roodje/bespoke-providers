package com.yolt.providers.knabgroup.common.payment.status;

import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentExecutionHttpHeadersProvider;
import com.yolt.providers.knabgroup.common.payment.DefaultCommonPaymentHttpHeadersProvider;
import com.yolt.providers.knabgroup.common.payment.dto.Internal.StatusPaymentPreExecutionResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;

@RequiredArgsConstructor
public class DefaultStatusPaymentExecutionHttpHeadersProvider implements PaymentExecutionHttpHeadersProvider<StatusPaymentPreExecutionResult, Void> {

    private final DefaultCommonPaymentHttpHeadersProvider commonHttpHeadersProvider;

    @Override
    public HttpHeaders provideHttpHeaders(final StatusPaymentPreExecutionResult preExecutionResult, final Void paymentBody) {
        return commonHttpHeadersProvider.provideHttpHeaders(
                preExecutionResult.getAccessToken(),
                preExecutionResult.getAuthenticationMeans().getSigningData(preExecutionResult.getSigner()),
                new byte[]{},
                preExecutionResult.getPsuIpAddress());
    }
}
