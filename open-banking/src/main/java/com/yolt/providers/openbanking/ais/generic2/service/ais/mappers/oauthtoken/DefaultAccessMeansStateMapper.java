package com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.oauthtoken;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.exception.UnexpectedJsonElementException;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeansState;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.util.List;

@AllArgsConstructor
@Slf4j
public class DefaultAccessMeansStateMapper<T extends AccessMeansState> implements AccessMeansStateMapper<T> {
    private final ObjectMapper objectMapper;
    private final TypeReference<T> type;

    public DefaultAccessMeansStateMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        type = new TypeReference<T>() {
        };
    }

    @Override
    public T fromJson(String accessMeans) throws TokenInvalidException {
        try {
            return objectMapper.readValue(accessMeans, type);
        } catch (IOException e) {
            throw new TokenInvalidException("Unable to deserialize access means state");
        }
    }

    @Override
    public String toJson(T accessMeansState) {
        try {
            return objectMapper.writeValueAsString(accessMeansState);
        } catch (JsonProcessingException e) {
            throw new UnexpectedJsonElementException("Unable to serialize access means state");
        }
    }
}