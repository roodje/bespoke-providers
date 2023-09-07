package com.yolt.providers.stet.generic.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.yolt.providers.stet.generic.mapper.DateTimeSupplier;
import org.springframework.boot.jackson.JsonComponentModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.io.IOException;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.Objects;

@Configuration
public class DefaultBeanConfig {

    private final DateTimeSupplier dateTimeSupplier;

    public DefaultBeanConfig(Clock clock) {
        dateTimeSupplier = new DateTimeSupplier(clock);
    }

    @Bean("StetObjectMapper")
    public ObjectMapper getObjectMapper() {
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
        builder.deserializerByType(OffsetDateTime.class, new OffsetDateTimeDeserializer());
        builder.serializerByType(OffsetDateTime.class, new OffsetDateTimeSerializer());

        return builder.build()
                .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
                .setDateFormat(new StdDateFormat().withColonInTimeZone(false))
                .registerModule(new JavaTimeModule())
                .registerModule(new JsonComponentModule())
                .setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    private static class OffsetDateTimeSerializer extends JsonSerializer<OffsetDateTime> {

        private final DateTimeFormatter dateTimeFormatter = new DateTimeFormatterBuilder()
                .appendPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
                .toFormatter();

        @Override
        public void serialize(OffsetDateTime offsetDateTime, JsonGenerator generator, SerializerProvider provider) throws IOException {
            try {
                generator.writeString(offsetDateTime.format(dateTimeFormatter));
            } catch (DateTimeParseException e) {
                generator.writeString("");
            }
        }
    }

    private class OffsetDateTimeDeserializer extends JsonDeserializer<OffsetDateTime> {


        private static final DateTimeFormatter DATE_TIME_FORMATTER = new DateTimeFormatterBuilder()
                .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX"))
                .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"))
                .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ"))
                .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX"))
                .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ"))
                .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSZ"))
                .toFormatter();

        @Override
        public OffsetDateTime deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            if (JsonToken.START_OBJECT.equals(parser.currentToken()) || Objects.isNull(parser.getValueAsString())) {
                parser.nextToken();
            }
            try {
                return OffsetDateTime.parse(parser.getValueAsString(), DATE_TIME_FORMATTER);
            } catch (DateTimeParseException e) {
                return dateTimeSupplier.parseToOffsetDateTime(
                        parser.getValueAsString(),
                        DateTimeFormatter.ofPattern("[yyyy-MM-dd][yyyy/MM/dd][dd-MM-yyyy][dd/MM/yyyy]"));
            }
        }
    }
}
