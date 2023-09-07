package com.yolt.providers.ing.common.pec;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.initiate.SepaPaymentProviderStateExtractor;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentIdExtractor;
import com.yolt.providers.ing.common.dto.PaymentProviderState;
import com.yolt.providers.ing.common.exception.ProviderStateSerializationException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CommonProviderStateExtractorV2<HttpResponseBody, PreExecutionResult> implements SepaPaymentProviderStateExtractor<HttpResponseBody, PreExecutionResult> {

    private final ObjectMapper objectMapper;
    private final PaymentIdExtractor<HttpResponseBody, PreExecutionResult> paymentIdExtractor;
    private final PaymentTypeExtractor<HttpResponseBody, PreExecutionResult> paymentTypeExtractor;

    @Override
    public String extractProviderState(HttpResponseBody httpResponseBody, PreExecutionResult preExecutionResult) {
        PaymentProviderState providerState = new PaymentProviderState(
                paymentIdExtractor.extractPaymentId(httpResponseBody, preExecutionResult),
                paymentTypeExtractor.extractPaymentType(httpResponseBody, preExecutionResult));
        try {
            return objectMapper.writeValueAsString(providerState);
        } catch (JsonProcessingException e) {
            throw new ProviderStateSerializationException("Cannot serialize provider state", e);
        }
    }
}
