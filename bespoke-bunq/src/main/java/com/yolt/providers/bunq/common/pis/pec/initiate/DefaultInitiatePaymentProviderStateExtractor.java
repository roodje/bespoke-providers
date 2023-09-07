package com.yolt.providers.bunq.common.pis.pec.initiate;

import com.bunq.sdk.security.SecurityUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.bunq.common.model.PaymentServiceProviderDraftPaymentResponse;
import com.yolt.providers.bunq.common.pis.pec.PaymentProviderState;
import com.yolt.providers.bunq.common.pis.pec.exception.ProviderStateSerializationException;
import com.yolt.providers.common.pis.common.PaymentType;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.initiate.SepaPaymentProviderStateExtractor;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DefaultInitiatePaymentProviderStateExtractor implements SepaPaymentProviderStateExtractor<PaymentServiceProviderDraftPaymentResponse, DefaultInitiatePaymentPreExecutionResult> {

    private final PaymentType paymentType;
    private final ObjectMapper objectMapper;

    @Override
    public String extractProviderState(PaymentServiceProviderDraftPaymentResponse response, DefaultInitiatePaymentPreExecutionResult preExecutionResult) {
        var providerState = new PaymentProviderState(response.getResponse().get(0).getId().getId(),
                paymentType,
                preExecutionResult.getSessionToken(),
                preExecutionResult.getExpirationTime(),
                SecurityUtils.getPublicKeyFormattedString(preExecutionResult.getKeyPair()),
                SecurityUtils.getPrivateKeyFormattedString(preExecutionResult.getKeyPair()));
        return serializeProviderState(providerState);
    }

    private String serializeProviderState(PaymentProviderState providerState) {
        try {
            return objectMapper.writeValueAsString(providerState);
        } catch (JsonProcessingException e) {
            throw new ProviderStateSerializationException("Cannot serialize provider state", e);
        }
    }
}
