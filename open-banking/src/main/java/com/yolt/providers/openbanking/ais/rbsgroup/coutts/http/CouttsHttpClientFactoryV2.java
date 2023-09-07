package com.yolt.providers.openbanking.ais.rbsgroup.coutts.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.openbanking.ais.generic2.DefaultProperties;
import com.yolt.providers.openbanking.ais.rbsgroup.common.http.RbsGroupHttpClientFactoryV2;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

public class CouttsHttpClientFactoryV2 extends RbsGroupHttpClientFactoryV2 {

    public CouttsHttpClientFactoryV2(DefaultProperties properties, MeterRegistry registry, ObjectMapper mapper) {
        super(properties, registry, mapper);
    }

    @Override
    protected HttpMessageConverter[] getHttpMessageConverters(ObjectMapper mapper) {
        return new HttpMessageConverter[]{
                new MappingJackson2HttpMessageConverter(mapper),
                new FormHttpMessageConverter(),
                new ByteArrayHttpMessageConverter(),
                new StringHttpMessageConverter()
        };
    }
}
