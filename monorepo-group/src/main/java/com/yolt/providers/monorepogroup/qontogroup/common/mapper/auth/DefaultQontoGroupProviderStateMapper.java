package com.yolt.providers.monorepogroup.qontogroup.common.mapper.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.monorepogroup.qontogroup.common.dto.internal.QontoGroupProviderState;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DefaultQontoGroupProviderStateMapper implements QontoGroupProviderStateMapper {

    private final ObjectMapper objectMapper;

    @Override
    public String serialize(QontoGroupProviderState providerState) throws ProviderStateProcessingException {
        try {
            return objectMapper.writeValueAsString(providerState);
        } catch (JsonProcessingException e) {
            throw new ProviderStateProcessingException("Unable to serialize provider state", e);
        }
    }

    @Override
    public QontoGroupProviderState deserialize(String serializedProviderState) throws ProviderStateProcessingException {
        try {
            return objectMapper.readValue(serializedProviderState, QontoGroupProviderState.class);
        } catch (JsonProcessingException e) {
            throw new ProviderStateProcessingException("Unable to deserialize provider state", e);
        }
    }
}
