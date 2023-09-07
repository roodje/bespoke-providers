package com.yolt.providers.argentagroup.common.service.token;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.argentagroup.common.exception.MalformedObjectException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AccessMeansMapper {

    private final ObjectMapper objectMapper;

    public String serializeAccessMeans(final AccessMeans accessMeans) {
        try {
            return objectMapper.writeValueAsString(accessMeans);
        } catch (JsonProcessingException e) {
            throw new MalformedObjectException("Could not serialize access means");
        }
    }

    public AccessMeans deserializeAccessMeans(final String accessMeans) {
        try {
            return objectMapper.readValue(accessMeans, AccessMeans.class);
        } catch (JsonProcessingException e) {
            throw new MalformedObjectException("Could not deserialize access means");
        }
    }
}
