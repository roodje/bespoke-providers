package com.yolt.providers.argentagroup.common.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.argentagroup.common.exception.MalformedObjectException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ProviderStateMapper {

    private final ObjectMapper objectMapper;

    public String serializeProviderState(final ProviderState providerState) {
        try {
            return objectMapper.writeValueAsString(providerState);
        } catch (JsonProcessingException e) {
            throw new MalformedObjectException("Could not serialize provider state");
        }
    }

    public ProviderState deserializeProviderState(final String providerState) {
        try {
            return objectMapper.readValue(providerState, ProviderState.class);
        } catch (JsonProcessingException e) {
            throw new MalformedObjectException("Could not deserialize provider state");
        }
    }
}
