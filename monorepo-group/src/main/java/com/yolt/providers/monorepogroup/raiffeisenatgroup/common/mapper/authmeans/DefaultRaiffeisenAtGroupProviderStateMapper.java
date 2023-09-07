package com.yolt.providers.monorepogroup.raiffeisenatgroup.common.mapper.authmeans;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.dto.internal.RaiffeisenAtGroupProviderState;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DefaultRaiffeisenAtGroupProviderStateMapper implements RaiffeisenAtGroupProviderStateMapper {

    private final ObjectMapper objectMapper;

    @Override
    public String serialize(final RaiffeisenAtGroupProviderState providerState) throws ProviderStateProcessingException {
        try {
            return objectMapper.writeValueAsString(providerState);
        } catch (JsonProcessingException e) {
            throw new ProviderStateProcessingException("Unable to serialize provider state", e);
        }
    }

    @Override
    public RaiffeisenAtGroupProviderState deserialize(final String providerState) throws ProviderStateProcessingException {
        try {
            return objectMapper.readValue(providerState, RaiffeisenAtGroupProviderState.class);
        } catch (JsonProcessingException e) {
            throw new ProviderStateProcessingException("Unable to deserialize provider state", e);
        }
    }
}
