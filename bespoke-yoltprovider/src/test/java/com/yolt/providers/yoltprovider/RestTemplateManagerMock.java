package com.yolt.providers.yoltprovider;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.RestTemplateManagerConfiguration;
import com.yolt.providers.common.rest.ExternalRestTemplateBuilderFactory;
import org.springframework.web.client.RestTemplate;

import java.security.cert.X509Certificate;
import java.util.UUID;
import java.util.function.Function;

public class RestTemplateManagerMock implements RestTemplateManager {

    private final ExternalRestTemplateBuilderFactory restTemplateBuilderFactory = new ExternalRestTemplateBuilderFactory();

    @Override
    public RestTemplate manage(UUID privateTransportKid, X509Certificate[] clientCertificateChain, Function<ExternalRestTemplateBuilderFactory, RestTemplate> customizationFunction) {
        return customizationFunction.apply(restTemplateBuilderFactory);
    }

    @Override
    public RestTemplate manage(RestTemplateManagerConfiguration restTemplateManagerConfiguration) {
        return restTemplateManagerConfiguration.getCustomizationFunction().apply(restTemplateBuilderFactory);
    }
}
