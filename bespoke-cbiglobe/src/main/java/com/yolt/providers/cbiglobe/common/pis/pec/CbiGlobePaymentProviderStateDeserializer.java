package com.yolt.providers.cbiglobe.common.pis.pec;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.cbiglobe.common.pis.pec.exception.ProviderStateDeserializationException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CbiGlobePaymentProviderStateDeserializer {

    private final ObjectMapper objectMapper;

    public CbiGlobePaymentProviderState deserialize(String providerState) {
        try {
            return objectMapper.readValue(providerState, CbiGlobePaymentProviderState.class);
        } catch (JsonProcessingException e) {
            throw new ProviderStateDeserializationException("Cannot deserialize provider state", e);
        }
    }
}
