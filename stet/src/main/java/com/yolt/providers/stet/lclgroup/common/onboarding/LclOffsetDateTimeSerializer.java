package com.yolt.providers.stet.lclgroup.common.onboarding;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@RequiredArgsConstructor
class LclOffsetDateTimeSerializer extends JsonSerializer<OffsetDateTime> {

    private final DateTimeFormatter formatter;

    @Override
    public void serialize(OffsetDateTime dateTime, JsonGenerator gen, SerializerProvider provider) throws IOException {
        try {
            String formattedDateTime = dateTime.format(formatter);
            gen.writeString(formattedDateTime);
        } catch (DateTimeParseException e) {
            gen.writeString("");
        }
    }
}