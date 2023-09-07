package com.yolt.providers.axabanque.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GroupBeanConfig {

    @Bean("AxaGroupObjectMapper")
    public ObjectMapper getGroupObjectMapper() {
        return new ObjectMapper();
    }
}
