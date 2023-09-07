package com.yolt.providers.argentagroup;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.RestTemplateManagerConfiguration;
import com.yolt.providers.common.rest.ExternalRestTemplateBuilderFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestTemplate;

import java.security.cert.X509Certificate;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@RequiredArgsConstructor
public class RestTemplateManagerMock implements RestTemplateManager {

    private final ExternalRestTemplateBuilderFactory externalRestTemplateBuilderFactory;

    @Override
    public RestTemplate manage(UUID privateTransportKid, X509Certificate[] clientCertificateChain, Function<ExternalRestTemplateBuilderFactory, RestTemplate> customizationFunction) {
        return customizationFunction.apply(externalRestTemplateBuilderFactory);
    }

    @Override
    public RestTemplate manage(final RestTemplateManagerConfiguration restTemplateManagerConfiguration) {
        return restTemplateManagerConfiguration.getCustomizationFunction().apply(externalRestTemplateBuilderFactory);
    }
}
