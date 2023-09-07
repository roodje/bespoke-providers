package com.yolt.providers.stet.lclgroup.common.onboarding;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

//TODO: Remove this class during migration to DefaultHttpClient (C4PO-6616)
class LclLocalDateJsonDeserializer extends JsonDeserializer<LocalDate> {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
            .toFormatter();

    public static MappingJackson2HttpMessageConverter customizedJacksonMapper() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(OffsetDateTime.class, new LclOffsetDateTimeSerializer(DATE_TIME_FORMATTER));
        module.addDeserializer(OffsetDateTime.class, new LclOffsetDateTimeDeserializer(DATE_TIME_FORMATTER));

        Jackson2ObjectMapperBuilder jacksonObjectMapperBuilder = new Jackson2ObjectMapperBuilder();
        jacksonObjectMapperBuilder.deserializerByType(LocalDate.class, new LclLocalDateJsonDeserializer());
        jacksonObjectMapperBuilder.serializationInclusion(JsonInclude.Include.NON_NULL);
        jacksonObjectMapperBuilder.modules(module);
        return new MappingJackson2HttpMessageConverter(jacksonObjectMapperBuilder.build());
    }

    @Override
    public LocalDate deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        if (jsonParser.currentToken() == JsonToken.START_OBJECT || jsonParser.getValueAsString() == null) {
            jsonParser.nextToken();
        }
        // Make sure the date is not in date-time format and contain only slashes
        String value = jsonParser.getValueAsString().substring(0, 10).replace("-", "/");
        // Check whether date starts with a day or year
        String pattern = value.indexOf('/') == 4 ? "yyyy/MM/dd" : "dd/MM/yyyy";
        return LocalDate.parse(value, DateTimeFormatter.ofPattern(pattern));
    }
}