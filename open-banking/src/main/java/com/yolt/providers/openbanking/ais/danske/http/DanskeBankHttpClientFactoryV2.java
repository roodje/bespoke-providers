package com.yolt.providers.openbanking.ais.danske.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.openbanking.ais.generic2.DefaultProperties;
import com.yolt.providers.openbanking.ais.generic2.http.DefaultHttpClientFactory;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import static java.nio.charset.StandardCharsets.UTF_8;

public class DanskeBankHttpClientFactoryV2 extends DefaultHttpClientFactory {

    public DanskeBankHttpClientFactoryV2(final DefaultProperties properties,
                                         final MeterRegistry registry,
                                         final ObjectMapper mapper) {
        super(properties, registry, mapper);
    }

    @Override
    protected HttpMessageConverter[] getHttpMessageConverters(final ObjectMapper mapper) {
        MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter(mapper);
        mappingJackson2HttpMessageConverter.setDefaultCharset(UTF_8);
        return new HttpMessageConverter[]{
                mappingJackson2HttpMessageConverter,
                new FormHttpMessageConverter(),
                new StringHttpMessageConverter(),
                new ByteArrayHttpMessageConverter()
        };
    }

    @Override
    protected ClientHttpRequestInterceptor[] getClientHttpRequestInterceptors() {
        return new ClientHttpRequestInterceptor[]{
                new DanskeContentTypeHeaderInterceptor()
        };
    }
}
