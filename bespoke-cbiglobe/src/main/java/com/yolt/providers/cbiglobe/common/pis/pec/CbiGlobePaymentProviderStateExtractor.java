package com.yolt.providers.cbiglobe.common.pis.pec;

import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.initiate.SepaPaymentProviderStateExtractor;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentIdExtractor;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CbiGlobePaymentProviderStateExtractor<HttpResponseBody, PreExecutionResult> implements SepaPaymentProviderStateExtractor<HttpResponseBody, PreExecutionResult> {

    private final CbiGlobePaymentProviderStateSerializer providerStateSerializer;
    private final PaymentIdExtractor<HttpResponseBody, PreExecutionResult> paymentIdExtractor;

    @Override
    public String extractProviderState(HttpResponseBody httpResponseBody, PreExecutionResult preExecutionResult) {
        return providerStateSerializer.serialize(new CbiGlobePaymentProviderState(paymentIdExtractor.extractPaymentId(httpResponseBody, preExecutionResult)));
    }
}
