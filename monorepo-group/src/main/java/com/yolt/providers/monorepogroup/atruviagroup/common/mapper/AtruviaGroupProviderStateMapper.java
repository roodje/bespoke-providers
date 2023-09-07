package com.yolt.providers.monorepogroup.atruviagroup.common.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class AtruviaGroupProviderStateMapper {

    private final ObjectMapper objectMapper;

    public String toJson(Object state) {
        try {
            return objectMapper.writeValueAsString(state);
        } catch (JsonProcessingException e) {
            throw new GetAccessTokenFailedException("Unable to serialize provider state");
        }
    }

    public <T> T fromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new GetAccessTokenFailedException("Unable to deserialize provider state");
        }
    }
}
