package com.yolt.providers.fineco.pis.status;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.initiate.SepaPaymentProviderStateExtractor;
import com.yolt.providers.fineco.dto.PaymentProviderState;
import com.yolt.providers.fineco.dto.PaymentResponse;
import com.yolt.providers.fineco.exception.ProviderStateSerializationException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FinecoStatusProviderStateExtractor implements SepaPaymentProviderStateExtractor<PaymentResponse, FinecoStatusPaymentPreExecutionResult> {

    private final ObjectMapper objectMapper;

    @Override
    public String extractProviderState(PaymentResponse initiatePaymentResponse, FinecoStatusPaymentPreExecutionResult preExecutionResult) {
        PaymentProviderState providerState = new PaymentProviderState(
                initiatePaymentResponse.getPaymentId(),
                preExecutionResult.getPaymentType());
        try {
            return objectMapper.writeValueAsString(providerState);
        } catch (JsonProcessingException e) {
            throw new ProviderStateSerializationException("Cannot serialize provider state", e);
        }
    }
}
