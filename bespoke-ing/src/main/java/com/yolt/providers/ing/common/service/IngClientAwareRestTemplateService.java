package com.yolt.providers.ing.common.service;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.RestTemplateManagerConfiguration;
import com.yolt.providers.ing.common.auth.IngAuthenticationMeans;
import com.yolt.providers.ing.common.config.IngProperties;
import org.springframework.data.web.ProjectingJackson2HttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

public class IngClientAwareRestTemplateService {

    private final IngProperties properties;

    public IngClientAwareRestTemplateService(final IngProperties properties) {
        this.properties = properties;
    }

    public RestTemplate buildRestTemplate(final IngAuthenticationMeans authenticationMeans,
                                          final RestTemplateManager restTemplateManager) {
        RestTemplate restTemplate = restTemplateManager.manage(new RestTemplateManagerConfiguration(
                authenticationMeans.getTransportKeyId(),
                authenticationMeans.getTlsCertificate(),
                externalRestTemplateBuilderFactory -> externalRestTemplateBuilderFactory
                        .messageConverters(
                                new ProjectingJackson2HttpMessageConverter(),
                                new FormHttpMessageConverter())
                        .rootUri(properties.getBaseUrl())
                        .build()));
        DefaultUriBuilderFactory defaultUriBuilderFactory = new DefaultUriBuilderFactory(properties.getBaseUrl());
        defaultUriBuilderFactory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.NONE);
        restTemplate.setUriTemplateHandler(defaultUriBuilderFactory);
        return restTemplate;
    }
}