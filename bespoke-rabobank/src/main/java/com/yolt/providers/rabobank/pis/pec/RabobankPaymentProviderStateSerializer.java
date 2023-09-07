package com.yolt.providers.rabobank.pis.pec;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.rabobank.pis.pec.exception.ProviderStateSerializationException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RabobankPaymentProviderStateSerializer {

    private final ObjectMapper objectMapper;

    public String serialize(RabobankPaymentProviderState providerState) {
        try {
            return objectMapper.writeValueAsString(providerState);
        } catch (JsonProcessingException e) {
            throw new ProviderStateSerializationException("Cannot serialize provider state", e);
        }
    }
}
