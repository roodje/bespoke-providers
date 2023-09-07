package com.yolt.providers.consorsbankgroup.common.ais;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.consorsbankgroup.common.ais.exception.UnexpectedJsonElementException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AccessMeansMapper {

    private final ObjectMapper objectMapper;

    public DefaultAccessMeans readAccessMeans(final String serializedAccessMeans) {
        DefaultAccessMeans accessMeans;
        try {
            accessMeans = objectMapper.readValue(serializedAccessMeans, DefaultAccessMeans.class);
        } catch (JsonProcessingException e) {
            throw new UnexpectedJsonElementException("Unable to deserialize accessMeans");
        }
        return accessMeans;
    }

    public String serializeAccessMeans(final DefaultAccessMeans accessMeans) {
        try {
            return objectMapper.writeValueAsString(accessMeans);
        } catch (JsonProcessingException e) {
            throw new UnexpectedJsonElementException("Unable to serialize accessMeans");
        }
    }
}
