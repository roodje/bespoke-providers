package com.yolt.providers.triodosbank;

import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.yolt.providers.FakeRestTemplateManager;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.rest.ExternalRestTemplateBuilderFactory;
import com.yolt.providers.common.rest.YoltProxySelectorBuilder;
import lombok.RequiredArgsConstructor;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.cloud.contract.wiremock.WireMockConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.security.Security;
import java.time.Clock;

@Configuration
@RequiredArgsConstructor
public class TestConfiguration {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    WireMockConfigurationCustomizer optionsCustomizer() {
        return options -> options
                .extensions(new ResponseTemplateTransformer(true))
                .keystoreType("pkcs12")
                .keystorePath("src/test/resources/certificates/fake-keystore.p12")
                .keystorePassword("changeit")
                .keyManagerPassword("changeit");
    }

    @Bean
    YoltProxySelectorBuilder getYoltProxySelectorBuilder() {
        return new YoltProxySelectorBuilder("Dummy", 0);
    }

    @Bean
    RestTemplateManager getRestTemplateManager(ExternalRestTemplateBuilderFactory factory) {
        return new FakeRestTemplateManager(factory);
    }

    @Bean
    ExternalRestTemplateBuilderFactory getExternalRestTemplateBuilderFactory() {
        ExternalRestTemplateBuilderFactory externalRestTemplateBuilderFactory = new ExternalRestTemplateBuilderFactory();
        externalRestTemplateBuilderFactory.requestFactory(SimpleClientHttpRequestFactory::new);
        return externalRestTemplateBuilderFactory;
    }
}
