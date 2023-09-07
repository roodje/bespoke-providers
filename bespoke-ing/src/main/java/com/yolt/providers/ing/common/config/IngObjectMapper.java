package com.yolt.providers.ing.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class IngObjectMapper {

    private static final ObjectMapper INSTANCE = new ObjectMapper();

    public static ObjectMapper get() {
        return INSTANCE;
    }
}
