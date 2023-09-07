package com.yolt.providers.abnamrogroup.common.pis.pec;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.abnamrogroup.common.pis.pec.exception.ProviderStateDeserializationException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AbnAmroProviderStateDeserializer {

    private final ObjectMapper objectMapper;

    public AbnAmroPaymentProviderState deserialize(String providerState) {
        try {
            return objectMapper.readValue(providerState, AbnAmroPaymentProviderState.class);
        } catch (JsonProcessingException e) {
            throw new ProviderStateDeserializationException("Cannot deserialize provider state", e);
        }
    }
}
