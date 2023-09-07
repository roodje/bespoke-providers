package com.yolt.providers.ing.common.pec.submit;

import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentExecutionHttpHeadersProvider;
import com.yolt.providers.ing.common.pec.DefaultCommonHttpHeadersProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;

import static com.yolt.providers.ing.common.pec.IngPecConstants.SUBMIT_PAYMENT_ENDPOINT;
import static com.yolt.providers.ing.common.pec.IngPecConstants.SUBMIT_PAYMENT_HTTP_METHOD;

/**
 * {@inheritDoc}
 *
 * @deprecated Use {@link DefaultSubmitPaymentHttpHeadersProviderV2} instead
 */
@Deprecated
@RequiredArgsConstructor
public class DefaultSubmitPaymentHttpHeadersProvider implements PaymentExecutionHttpHeadersProvider<DefaultSubmitPaymentPreExecutionResult, Void> {

    private final DefaultCommonHttpHeadersProvider commonHttpHeadersProvider;

    @Override
    public HttpHeaders provideHttpHeaders(final DefaultSubmitPaymentPreExecutionResult preExecutionResult, final Void unused) {
        String expandedEndpointPath = String.format(SUBMIT_PAYMENT_ENDPOINT, preExecutionResult.getPaymentId());

        return commonHttpHeadersProvider.provideHttpHeaders(preExecutionResult, new byte[0], SUBMIT_PAYMENT_HTTP_METHOD, expandedEndpointPath);
    }
}
