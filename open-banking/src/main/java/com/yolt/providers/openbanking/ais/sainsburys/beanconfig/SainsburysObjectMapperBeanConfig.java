package com.yolt.providers.openbanking.ais.sainsburys.beanconfig;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

@Configuration
public class SainsburysObjectMapperBeanConfig {

    @Bean
    @Qualifier("SainsburysObjectMapper")
    public ObjectMapper getSainsburysObjectMapper(Jackson2ObjectMapperBuilder mapperBuilder) {
        DateTimeFormatter sainsburysFormatter = new DateTimeFormatterBuilder()
                .appendPattern("yyyy-MM-dd'T'HH:mm:ss")
                .appendPattern("x")
                .toFormatter();
        ObjectMapper mapper = mapperBuilder
                .deserializerByType(OffsetDateTime.class, new DateTimeJsonDeserializer(DateTimeFormatter.ISO_DATE_TIME, sainsburysFormatter))
                .build();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        return mapper;
    }
}
