package com.yolt.providers.stet.generic.service.pec.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.initiate.SepaPaymentProviderStateExtractor;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentIdExtractor;
import com.yolt.providers.stet.generic.domain.PaymentProviderState;
import com.yolt.providers.stet.generic.exception.ProviderStateMalformedException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StetPaymentProviderStateExtractor<HttpResponseBody, PreExecutionResult> implements SepaPaymentProviderStateExtractor<HttpResponseBody, PreExecutionResult> {

    private final PaymentIdExtractor<HttpResponseBody, PreExecutionResult> paymentIdExtractor;
    private final ObjectMapper objectMapper;

    @Override
    public String extractProviderState(HttpResponseBody httpResponseBody, PreExecutionResult preExecutionResult) {
        String paymentId = paymentIdExtractor.extractPaymentId(httpResponseBody, preExecutionResult);
        PaymentProviderState providerState = PaymentProviderState.initiatedProviderState(paymentId);
        try {
            return objectMapper.writeValueAsString(providerState);
        } catch (JsonProcessingException e) {
            throw new ProviderStateMalformedException("Unable to serialize payment provider state", e);
        }
    }

    public PaymentProviderState mapToPaymentProviderState(String jsonProviderState) {
        try {
            return objectMapper.readValue(jsonProviderState, PaymentProviderState.class);
        } catch (JsonProcessingException e) {
            throw new ProviderStateMalformedException("Unable to deserialize payment provider state", e);
        }
    }
}
