package com.yolt.providers.fineco.pis.initiate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.pis.common.PaymentType;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.initiate.SepaPaymentProviderStateExtractor;
import com.yolt.providers.fineco.dto.PaymentProviderState;
import com.yolt.providers.fineco.dto.PaymentResponse;
import com.yolt.providers.fineco.exception.ProviderStateSerializationException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FinecoInitiateProviderStateExtractor implements SepaPaymentProviderStateExtractor<PaymentResponse, FinecoInitiatePaymentPreExecutionResult> {

    private final ObjectMapper objectMapper;
    private final PaymentType paymentType;

    @Override
    public String extractProviderState(PaymentResponse initiatePaymentResponse, FinecoInitiatePaymentPreExecutionResult preExecutionResult) {
        PaymentProviderState providerState = new PaymentProviderState(
                initiatePaymentResponse.getPaymentId(),
                paymentType);
        try {
            return objectMapper.writeValueAsString(providerState);
        } catch (JsonProcessingException e) {
            throw new ProviderStateSerializationException("Cannot serialize provider state", e);
        }
    }
}
