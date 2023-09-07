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
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@RequiredArgsConstructor
public class BunqPisHttpClientFactory {

    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;
    private final BunqProperties properties;

    public BunqPisHttpClient createHttpClient(final RestTemplateManager restTemplateManager,
                                              final String providerDisplayName) {
        RestTemplate restTemplate = restTemplateManager.manage(
                new RestTemplateManagerConfiguration(
                        externalRestTemplateBuilderFactory ->
                                externalRestTemplateBuilderFactory
                                        .rootUri(properties.getBaseUrl())
                                        .messageConverters(getHttpMessageConverters())
                                        .build())
        );

        return new BunqPisHttpClient(meterRegistry, restTemplate, providerDisplayName, BunqHttpErrorHandler.BUNQ_HTTP_ERROR_HANDLER);
    }

    private HttpMessageConverter[] getHttpMessageConverters() {
        StringHttpMessageConverter stringHttpMessageConverter = new StringHttpMessageConverter();
        stringHttpMessageConverter.setSupportedMediaTypes(Collections.singletonList(MediaType.APPLICATION_JSON));
        return new HttpMessageConverter[]{
                new ProjectingJackson2HttpMessageConverter(objectMapper), new MappingJackson2HttpMessageConverter(objectMapper), stringHttpMessageConverter
        };
    }
}