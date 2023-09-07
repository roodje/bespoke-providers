package com.yolt.providers.openbanking.ais.virginmoney2group.common.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.openbanking.ais.generic2.DefaultProperties;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.http.DefaultHttpClientFactory;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

public class VirginMoney2GroupHttpClientFactory extends DefaultHttpClientFactory {

    private final DefaultProperties properties;

    public VirginMoney2GroupHttpClientFactory(DefaultProperties properties, MeterRegistry registry, ObjectMapper mapper) {
        super(properties, registry, mapper);
        this.properties = properties;
    }

    @Override
    protected RestTemplate getManagedRestTemplate(RestTemplateManager restTemplateManager, DefaultAuthMeans authenticationMeans) {
        RestTemplate restTemplate = super.getManagedRestTemplate(restTemplateManager, authenticationMeans);
        DefaultUriBuilderFactory defaultUriBuilderFactory = new DefaultUriBuilderFactory(properties.getBaseUrl());
        defaultUriBuilderFactory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.NONE);
        restTemplate.setUriTemplateHandler(defaultUriBuilderFactory);
        return restTemplate;
    }
}
