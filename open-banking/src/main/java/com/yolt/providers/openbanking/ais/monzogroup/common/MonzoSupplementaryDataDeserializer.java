package com.yolt.providers.openbanking.ais.monzogroup.common;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.openbanking.ais.monzogroup.common.dto.SupplementaryDataV2;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBSupplementaryData1;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

@RequiredArgsConstructor
public class MonzoSupplementaryDataDeserializer extends JsonDeserializer<OBSupplementaryData1> {

    private final ObjectMapper objectMapper;

    @Override
    public OBSupplementaryData1 deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        return objectMapper.readValue(parser, SupplementaryDataV2.class);
    }
}