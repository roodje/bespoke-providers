package com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.oauthtoken;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.exception.UnexpectedJsonElementException;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import lombok.AllArgsConstructor;

import java.io.IOException;

@AllArgsConstructor
@Deprecated //Use DefaultAccessMeansStateMapper C4PO-8398
public class DefaultAccessMeansMapper<T extends AccessMeans> implements AccessMeansMapper<T> {
    private final ObjectMapper objectMapper;
    private final Class<T> type;

    public DefaultAccessMeansMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        type = (Class<T>) AccessMeans.class;
    }

    @Override
    public T fromJson(String accessMeans) throws TokenInvalidException {
        try {
            return objectMapper.readValue(accessMeans, type);
        } catch (IOException e) {
            throw new TokenInvalidException("Unable to deserialize access means");
        }
    }

    @Override
    public String toJson(T accessMeans) {
        try {
            return objectMapper.writeValueAsString(accessMeans);
        } catch (JsonProcessingException e) {
            throw new UnexpectedJsonElementException("Unable to serialize oAuthToken");
        }
    }
}