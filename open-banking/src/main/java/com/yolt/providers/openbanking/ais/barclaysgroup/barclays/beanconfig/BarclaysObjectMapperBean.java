package com.yolt.providers.openbanking.ais.barclaysgroup.barclays.beanconfig;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.openbanking.ais.barclaysgroup.common.BarclaysCreditDebitDeserializer;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBCreditDebitCode2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@Configuration
public class BarclaysObjectMapperBean {
    @Bean("BarclaysObjectMapperV2")
    public ObjectMapper getBarclaysObjectMapper(Jackson2ObjectMapperBuilder mapperBuilder) {
        ObjectMapper mapper = mapperBuilder
                .deserializerByType(OBCreditDebitCode2.class, new BarclaysCreditDebitDeserializer())
                .build();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper;
    }
}
