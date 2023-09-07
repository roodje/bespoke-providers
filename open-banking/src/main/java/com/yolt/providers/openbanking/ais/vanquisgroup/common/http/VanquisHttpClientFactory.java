package com.yolt.providers.openbanking.ais.vanquisgroup.common.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.openbanking.ais.generic2.http.DefaultHttpClientFactory;
import com.yolt.providers.openbanking.ais.vanquisgroup.vanquis.VanquisPropertiesV2;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import static java.nio.charset.StandardCharsets.UTF_8;

public class VanquisHttpClientFactory extends DefaultHttpClientFactory {

    public VanquisHttpClientFactory(final VanquisPropertiesV2 properties,
                                    final MeterRegistry registry,
                                    final ObjectMapper mapper) {
        super(properties, registry, mapper);
    }

    @Override
    protected HttpMessageConverter[] getHttpMessageConverters(final ObjectMapper mapper) {
        MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter(mapper);
        // This  default is set manually to keep the backward-compatibility with the Content-type sent on our requests (application/json;charset=UTF-8)
        // You MIGHT NOT NEED this in new connections:
        mappingJackson2HttpMessageConverter.setDefaultCharset(UTF_8);
        return new HttpMessageConverter[]{
                mappingJackson2HttpMessageConverter,
                new FormHttpMessageConverter(),
                new ByteArrayHttpMessageConverter()
        };
    }
}
