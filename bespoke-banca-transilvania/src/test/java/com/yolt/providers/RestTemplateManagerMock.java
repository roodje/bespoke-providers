package com.yolt.providers;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.RestTemplateManagerConfiguration;
import com.yolt.providers.common.rest.ExternalRestTemplateBuilderFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.web.client.RestTemplate;

import java.security.cert.X509Certificate;
import java.util.UUID;
import java.util.function.Function;

@RequiredArgsConstructor
public class RestTemplateManagerMock implements RestTemplateManager {

    private final ExternalRestTemplateBuilderFactory externalRestTemplateBuilderFactory;

    @Override
    public RestTemplate manage(UUID privateTransportKid, X509Certificate[] clientCertificateChain, Function<ExternalRestTemplateBuilderFactory, RestTemplate> customizationFunction) {
        return customizationFunction.apply(externalRestTemplateBuilderFactory);
    }

    @Override
    public RestTemplate manage(RestTemplateManagerConfiguration restTemplateManagerConfiguration) {
        return restTemplateManagerConfiguration.getCustomizationFunction().apply(externalRestTemplateBuilderFactory);
    }
}
