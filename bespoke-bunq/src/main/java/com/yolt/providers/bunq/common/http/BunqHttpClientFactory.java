package com.yolt.providers.bunq.common.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.bunq.common.configuration.BunqProperties;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.RestTemplateManagerConfiguration;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.data.web.ProjectingJackson2HttpMessageConverter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@RequiredArgsConstructor
public class BunqHttpClientFactory {

    private final MeterRegistry meterRegistry;
    private final ObjectMapper objectMapper;
    private final BunqProperties properties;
    private final BunqHttpHeaderProducer headerProducer;

    public BunqHttpClientV5 createHttpClient(RestTemplateManager restTemplateManager,
                                             String providerDisplayName) {
        RestTemplate restTemplate = restTemplateManager.manage(new RestTemplateManagerConfiguration(
                externalRestTemplateBuilderFactory -> externalRestTemplateBuilderFactory
                        .rootUri(properties.getBaseUrl())
                        .messageConverters(getHttpMessageConverters())
                        .build()));

        return new BunqHttpClientV5(meterRegistry, restTemplate, providerDisplayName, headerProducer, objectMapper);
    }

    private HttpMessageConverter[] getHttpMessageConverters() {
        StringHttpMessageConverter stringHttpMessageConverter = new StringHttpMessageConverter();
        stringHttpMessageConverter.setSupportedMediaTypes(Collections.singletonList(MediaType.APPLICATION_JSON));
        return new HttpMessageConverter[]{
                new ProjectingJackson2HttpMessageConverter(objectMapper), stringHttpMessageConverter
        };
    }
}