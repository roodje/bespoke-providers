package com.yolt.providers.rabobank.pis.pec;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.rabobank.pis.pec.exception.ProviderStateDeserializationException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RabobankPaymentProviderStateDeserializer {

    private final ObjectMapper objectMapper;

    public RabobankPaymentProviderState deserialize(String providerState) {
        try {
            return objectMapper.readValue(providerState, RabobankPaymentProviderState.class);
        } catch (JsonProcessingException e) {
            throw new ProviderStateDeserializationException("Cannot deserialize provider state", e);
        }
    }
}
