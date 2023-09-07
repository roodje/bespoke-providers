package com.yolt.providers.cbiglobe.common.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.cbiglobe.common.auth.CbiGlobeAuthenticationMeans;
import com.yolt.providers.cbiglobe.common.config.CbiGlobeBaseProperties;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.RestTemplateManagerConfiguration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.web.ProjectingJackson2HttpMessageConverter;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.UUID;

public class CbiGlobeHttpClientFactory {

    private final CbiGlobeBaseProperties properties;
    private final ObjectMapper objectMapper;

    public CbiGlobeHttpClientFactory(final CbiGlobeBaseProperties properties,
                                     @Qualifier("CbiGlobe") final ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public RestTemplate buildRestTemplate(RestTemplateManager restTemplateManager, CbiGlobeAuthenticationMeans authMeans) {
        return restTemplateManager.manage(new RestTemplateManagerConfiguration(
                UUID.fromString(authMeans.getTransportKeyId()),
                authMeans.getTransportCertificate(),
                externalRestTemplateBuilderFactory -> externalRestTemplateBuilderFactory
                        .rootUri(properties.getBaseUrl())
                        .messageConverters(Arrays.asList(
                                new ProjectingJackson2HttpMessageConverter(objectMapper),
                                new MappingJackson2HttpMessageConverter(objectMapper),
                                new FormHttpMessageConverter(),
                                new ByteArrayHttpMessageConverter()))
                        .build())
        );
    }
}
