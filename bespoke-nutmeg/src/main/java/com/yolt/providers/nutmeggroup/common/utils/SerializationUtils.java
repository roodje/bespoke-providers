package com.yolt.providers.nutmeggroup.common.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.nutmeggroup.common.dto.TokenResponse;
import lombok.experimental.UtilityClass;

import java.io.IOException;

@UtilityClass
public class SerializationUtils {

    private static final String SERIALIZATION_MESSAGE = "Unable to serialize token";
    private static final String DESERIALIZATION_MESSAGE = "Unable to deserialize access means";

    public static String toJson(final ObjectMapper objectMapper,
                                final TokenResponse accessMeans) throws TokenInvalidException {
        try {
            return objectMapper.writeValueAsString(accessMeans);
        } catch (JsonProcessingException e) {
            throw new TokenInvalidException(SERIALIZATION_MESSAGE);
        }
    }

    public static TokenResponse fromJson(final ObjectMapper objectMapper,
                                         final String accessMeans) throws TokenInvalidException {
        try {
            return objectMapper.readValue(accessMeans, TokenResponse.class);
        } catch (IOException e) {
            throw new TokenInvalidException(DESERIALIZATION_MESSAGE);
        }
    }
}
