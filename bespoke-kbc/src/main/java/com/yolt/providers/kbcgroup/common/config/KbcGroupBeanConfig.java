package com.yolt.providers.kbcgroup.common.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@Configuration
public class KbcGroupBeanConfig {

    @Bean("KbcGroupObjectMapper")
    public ObjectMapper getObjectMapper(final Jackson2ObjectMapperBuilder mapperBuilder) {
        ObjectMapper mapper = mapperBuilder.build();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper;
    }
}
