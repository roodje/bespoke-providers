package com.yolt.providers.volksbank.common.pis.pec;

import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.initiate.SepaPaymentProviderStateExtractor;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentIdExtractor;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class VolksbankPaymentProviderStateExtractor<HttpResponseBody, PreExecutionResult> implements SepaPaymentProviderStateExtractor<HttpResponseBody, PreExecutionResult> {

    private final VolksbankPaymentProviderStateSerializer providerStateSerializer;
    private final PaymentIdExtractor<HttpResponseBody, PreExecutionResult> paymentIdExtractor;

    @Override
    public String extractProviderState(HttpResponseBody httpResponseBody, PreExecutionResult preExecutionResult) {
        return providerStateSerializer.serialize(new VolksbankPaymentProviderState(paymentIdExtractor.extractPaymentId(httpResponseBody, preExecutionResult)));
    }
}
