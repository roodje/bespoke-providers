package com.yolt.providers.stet.cmarkeagroup.common.mapper;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.Objects;

@RequiredArgsConstructor
public class CmArkeaGroupObjectMapperFactory {

    private final ObjectMapper baseObjectMapper;
    private final ZoneId zoneId;

    public ObjectMapper getObjectMapper() {
        ObjectMapper objectMapper = baseObjectMapper.copy();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(OffsetDateTime.class, new DateTimeDeserializer(zoneId));
        objectMapper.registerModule(module);
        return objectMapper;
    }

    @RequiredArgsConstructor
    private class DateTimeDeserializer extends JsonDeserializer<OffsetDateTime> {

        private final ZoneId zoneId;

        @Override
        public OffsetDateTime deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            if (JsonToken.START_OBJECT.equals(parser.currentToken()) || Objects.isNull(parser.getValueAsString())) {
                parser.nextToken();
            }

            String dateStr = parser.getValueAsString();

            if (!StringUtils.hasText(dateStr)) {
                return null;
            }

            try {
                return OffsetDateTime.parse(parser.getValueAsString());
            }
            catch (DateTimeParseException e) {
                try {
                    //1.4.2 version returns datetime without offset
                    return LocalDateTime.parse(dateStr)
                            .atZone(zoneId)
                            .toOffsetDateTime();
                }
                catch (DateTimeParseException e2) {
                    //Try date
                    return LocalDate.parse(dateStr)
                            .atStartOfDay(zoneId)
                            .toOffsetDateTime();
                }
            }
        }
    }
}
