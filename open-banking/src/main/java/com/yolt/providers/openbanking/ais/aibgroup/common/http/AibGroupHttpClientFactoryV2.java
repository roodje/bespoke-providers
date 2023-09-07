package com.yolt.providers.openbanking.ais.aibgroup.common.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.openbanking.ais.generic2.DefaultProperties;
import com.yolt.providers.openbanking.ais.generic2.http.DefaultHttpClientFactory;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;

import java.util.stream.Stream;

public class AibGroupHttpClientFactoryV2 extends DefaultHttpClientFactory {


    public AibGroupHttpClientFactoryV2(DefaultProperties properties, MeterRegistry registry, ObjectMapper mapper) {
        super(properties, registry, mapper);
    }

    @Override
    protected HttpMessageConverter[] getHttpMessageConverters(ObjectMapper mapper) {
        return Stream.concat(
                Stream.of(super.getHttpMessageConverters(mapper)),
                Stream.of(new StringHttpMessageConverter())).toArray(HttpMessageConverter[]::new);
    }
}
