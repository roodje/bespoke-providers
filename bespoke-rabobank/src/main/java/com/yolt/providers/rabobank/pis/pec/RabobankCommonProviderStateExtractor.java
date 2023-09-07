package com.yolt.providers.rabobank.pis.pec;

import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.initiate.SepaPaymentProviderStateExtractor;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentIdExtractor;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RabobankCommonProviderStateExtractor<HttpResponseBody, PreExecutionResult> implements SepaPaymentProviderStateExtractor<HttpResponseBody, PreExecutionResult> {

    private final RabobankPaymentProviderStateSerializer providerStateSerializer;
    private final PaymentIdExtractor<HttpResponseBody, PreExecutionResult> paymentIdExtractor;

    @Override
    public String extractProviderState(HttpResponseBody httpResponseBody, PreExecutionResult preExecutionResult) {
        return providerStateSerializer.serialize(new RabobankPaymentProviderState(paymentIdExtractor.extractPaymentId(httpResponseBody, preExecutionResult)));
    }
}
