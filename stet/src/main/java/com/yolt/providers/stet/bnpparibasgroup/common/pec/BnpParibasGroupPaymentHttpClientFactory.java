package com.yolt.providers.stet.bnpparibasgroup.common.pec;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.yolt.providers.stet.bnpparibasgroup.common.exception.BnpParibasGroupUnsupportedMappingException;
import com.yolt.providers.stet.generic.http.client.DefaultHttpClientFactory;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.data.web.ProjectingJackson2HttpMessageConverter;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class BnpParibasGroupPaymentHttpClientFactory extends DefaultHttpClientFactory {

    private final ObjectMapper objectMapper;

    public BnpParibasGroupPaymentHttpClientFactory(MeterRegistry meterRegistry, ObjectMapper objectMapper) {
        super(meterRegistry, objectMapper);
        this.objectMapper = objectMapper;
    }

    @Override
    protected HttpMessageConverter[] getHttpMessageConverters() {
        return new HttpMessageConverter[]{
                new ByteArrayHttpMessageConverter(),
                new ProjectingJackson2HttpMessageConverter(objectMapper),
                new CustomMappingJackson2HttpMessageConverter(),
                new FormHttpMessageConverter()
        };
    }

    private class CustomMappingJackson2HttpMessageConverter extends MappingJackson2HttpMessageConverter {

        private CustomMappingJackson2HttpMessageConverter() {
            setObjectMapper(createObjectMapper());
        }

        private ObjectMapper createObjectMapper() {
            Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
            builder.deserializerByType(OffsetDateTime.class, new OffsetDateTimeDeserializer());

            ObjectMapper objectMapper = builder.build();
            objectMapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            objectMapper.setDateFormat(new StdDateFormat().withColonInTimeZone(true));

            return objectMapper;
        }

        private class OffsetDateTimeDeserializer extends JsonDeserializer<OffsetDateTime> {
            @Override
            public OffsetDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
                if (jsonParser.currentToken() == JsonToken.START_OBJECT || jsonParser.getValueAsString() == null) {
                    jsonParser.nextToken();
                }
                try {
                    return OffsetDateTime.parse(jsonParser.getValueAsString());
                } catch (DateTimeParseException e) {
                    if (e.getParsedString().matches("^\\d{4}(.*)$")) {
                        return toOffsetDateTime(jsonParser.getValueAsString(), DateTimeFormatter.ofPattern("yyyy/MM/dd"));

                    } else if (e.getParsedString().matches("^(.*)\\d{4}$")) {
                        return toOffsetDateTime(jsonParser.getValueAsString(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    }
                    throw new BnpParibasGroupUnsupportedMappingException("Unsupported date time format: " + e.getParsedString());
                }
            }
        }

        private OffsetDateTime toOffsetDateTime(String date, DateTimeFormatter formatter) {
            return LocalDate.parse(date.replace("-", "/"), formatter)
                    .atStartOfDay(ZoneId.of("Europe/Paris"))
                    .toOffsetDateTime();
        }
    }
}
