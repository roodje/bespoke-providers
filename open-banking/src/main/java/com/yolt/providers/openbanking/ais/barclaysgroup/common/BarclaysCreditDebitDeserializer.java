package com.yolt.providers.openbanking.ais.barclaysgroup.common;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBCreditDebitCode2;

import java.io.IOException;

public class BarclaysCreditDebitDeserializer extends JsonDeserializer<OBCreditDebitCode2> {
    @Override
    public OBCreditDebitCode2 deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        for (OBCreditDebitCode2 b : OBCreditDebitCode2.values()) {
            if (String.valueOf(b).equalsIgnoreCase(node.asText())) {
                return b;
            }
        }
        return null;
    }
}
