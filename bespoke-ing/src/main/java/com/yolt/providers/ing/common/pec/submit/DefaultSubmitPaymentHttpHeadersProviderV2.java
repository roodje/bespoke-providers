package com.yolt.providers.ing.common.pec.submit;

import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentExecutionHttpHeadersProvider;
import com.yolt.providers.ing.common.pec.DefaultCommonHttpHeadersProvider;
import com.yolt.providers.ing.common.pec.PaymentEndpointResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;

import static com.yolt.providers.ing.common.pec.IngPecConstants.SUBMIT_PAYMENT_HTTP_METHOD;

@RequiredArgsConstructor
public class DefaultSubmitPaymentHttpHeadersProviderV2 implements PaymentExecutionHttpHeadersProvider<DefaultSubmitPaymentPreExecutionResult, Void> {

    private final DefaultCommonHttpHeadersProvider commonHttpHeadersProvider;
    private final PaymentEndpointResolver endpointResolver;

    @Override
    public HttpHeaders provideHttpHeaders(final DefaultSubmitPaymentPreExecutionResult preExecutionResult, final Void unused) {
        String expandedEndpointPath = String.format(endpointResolver.getSubmitPaymentEndpoint(preExecutionResult.getPaymentType()), preExecutionResult.getPaymentId());

        return commonHttpHeadersProvider.provideHttpHeaders(preExecutionResult, new byte[0], SUBMIT_PAYMENT_HTTP_METHOD, expandedEndpointPath);
    }
}
