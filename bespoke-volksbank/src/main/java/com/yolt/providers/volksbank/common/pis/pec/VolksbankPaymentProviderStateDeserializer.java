package com.yolt.providers.volksbank.common.pis.pec;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.volksbank.common.pis.pec.exception.ProviderStateDeserializationException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class VolksbankPaymentProviderStateDeserializer {

    private final ObjectMapper objectMapper;

    public VolksbankPaymentProviderState deserialize(String providerState) {
        try {
            return objectMapper.readValue(providerState, VolksbankPaymentProviderState.class);
        } catch (JsonProcessingException e) {
            throw new ProviderStateDeserializationException("Cannot deserialize provider state", e);
        }
    }
}
