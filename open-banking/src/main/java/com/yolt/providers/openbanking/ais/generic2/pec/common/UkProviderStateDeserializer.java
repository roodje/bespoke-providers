package com.yolt.providers.openbanking.ais.generic2.pec.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.pis.ukdomestic.UkProviderState;
import com.yolt.providers.openbanking.ais.generic2.pec.common.exception.MalformedUkProviderStateException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UkProviderStateDeserializer {

    private final ObjectMapper objectMapper;

    public UkProviderState deserialize(String providerState) {
        try {
            return objectMapper.readValue(providerState, UkProviderState.class);
        } catch (JsonProcessingException ex) {
            throw new MalformedUkProviderStateException("Unable to parse UK provider state", ex);
        }
    }
}
