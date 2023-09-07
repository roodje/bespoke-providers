package com.yolt.providers.amexgroup;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.RestTemplateManagerConfiguration;
import com.yolt.providers.common.rest.ExternalRestTemplateBuilderFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.web.client.RestTemplate;

import java.security.cert.X509Certificate;
import java.util.UUID;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.fail;

@RequiredArgsConstructor
public class TestRestTemplateManager implements RestTemplateManager {

    private final ExternalRestTemplateBuilderFactory restTemplateBuilderFactory;
    private final String transportKeyIdRotation;

    @Override
    public RestTemplate manage(UUID privateTransportKid, X509Certificate[] clientCertificateChain, Function<ExternalRestTemplateBuilderFactory, RestTemplate> customizationFunction) {
        if (!privateTransportKid.toString().equals(transportKeyIdRotation)) {
            fail("Transport Certificate Key Identifier doesn't match");
        }
        return customizationFunction.apply(restTemplateBuilderFactory);
    }

    @Override
    public RestTemplate manage(RestTemplateManagerConfiguration restTemplateManagerConfiguration) {
        if (!restTemplateManagerConfiguration.getPrivateTransportKid().toString().equals(transportKeyIdRotation)) {
            fail("Transport Certificate Key Identifier doesn't match");
        }
        return restTemplateManagerConfiguration.getCustomizationFunction().apply(restTemplateBuilderFactory);
    }
}
