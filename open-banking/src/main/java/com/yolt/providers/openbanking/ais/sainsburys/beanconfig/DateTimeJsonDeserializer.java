package com.yolt.providers.openbanking.ais.sainsburys.beanconfig;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.yolt.providers.common.exception.JsonParseException;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

class DateTimeJsonDeserializer extends JsonDeserializer<OffsetDateTime> {

    private DateTimeFormatter[] formatters;

    public DateTimeJsonDeserializer(DateTimeFormatter... formatters) {
        this.formatters = formatters;
    }

    @Override
    public OffsetDateTime deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        DateTimeParseException lastException = null;
        for (DateTimeFormatter formatter : formatters) {
            try {
                return OffsetDateTime.parse(parser.getText(), formatter);
            } catch (DateTimeParseException e) {
                //Try with next formatter
                lastException = e;
            }
        }

        if (lastException != null) {
            throw lastException;
        } else {
            throw new JsonParseException("Date parse error", parser.getText());
        }
    }
}