package com.yolt.providers.knabgroup.common.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractBankSpecific {

    private static final ObjectMapper MAPPER = new ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    private static final MapType MAP_TYPE;

    static {
        MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        MAP_TYPE = MAPPER.getTypeFactory().constructMapType(HashMap.class, String.class, String.class);
    }

    /**
     * Takes all the fields in the class and serializes them as a map of
     * String -> JSON, where the keys are the field names
     */
    public Map<String, String> toMap() {
        return MAPPER.convertValue(this, MAP_TYPE);
    }
}