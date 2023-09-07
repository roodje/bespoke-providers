package com.yolt.providers.openbanking.ais.aibgroup;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBSupplementaryData1;

import java.io.IOException;

public class SupplementaryDataDeserializer extends JsonDeserializer<OBSupplementaryData1> {

    @Override
    public OBSupplementaryData1 deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        return new OBSupplementaryData1();
    }
}