package com.yolt.providers.nutmeggroup.common.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.RestTemplateManagerConfiguration;
import com.yolt.providers.common.domain.RestTemplateManagerConfigurationBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class NutmegGroupRestTemplateServiceV2 {

    private static final long KEEP_ALIVE_TIMEOUT_IN_MILLIS = 4000L;

    private final ObjectMapper objectMapper;

    public NutmegGroupRestTemplateServiceV2(@Qualifier("NutmegGroupObjectMapper") final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }


    public HttpClient createHttpClient(final RestTemplateManager restTemplateManager) {
        RestTemplateManagerConfiguration restTemplateManagerConfiguration = RestTemplateManagerConfigurationBuilder
                .nonMutualTlsBuilder(
                        externalRestTemplateBuilderFactory ->
                                externalRestTemplateBuilderFactory
                                        .messageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                                        .build())
                .defaultKeepAliveTimeoutInMillis(KEEP_ALIVE_TIMEOUT_IN_MILLIS)
                .build();
        RestTemplate restTemplate = restTemplateManager.manage(restTemplateManagerConfiguration);
        return new HttpClient(restTemplate);
    }
}