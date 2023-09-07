package com.yolt.providers.argentagroup;

import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.rest.ExternalRestTemplateBuilderFactory;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.MockClock;
import io.micrometer.core.instrument.simple.SimpleConfig;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.cloud.contract.wiremock.WireMockConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.security.Security;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

@Configuration
public class ArgentaGroupTestConfiguration {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Bean
    WireMockConfigurationCustomizer optionsCustomizer() {
        return options -> options
                .extensions(new ResponseTemplateTransformer(false))
                .keystoreType("pkcs12")
                .keystorePath("src/test/resources/certificates/fake-keystore.p12")
                .keystorePassword("changeit")
                .keyManagerPassword("changeit");
    }

    @Bean
    Clock testClock() {
        return Clock.fixed(Instant.parse("2021-12-31T00:00:00Z"), ZoneOffset.UTC);
    }

    @Bean
    MeterRegistry mockedMeterRegistry() {
        return new SimpleMeterRegistry(SimpleConfig.DEFAULT, new MockClock());
    }

    @Bean
    @Scope("prototype")
    RestTemplateManager getRestTemplateManagerMock() {
        return new RestTemplateManagerMock(getExternalRestTemplateBuilderFactory());
    }

    @Bean
    @Scope("prototype")
    ExternalRestTemplateBuilderFactory getExternalRestTemplateBuilderFactory() {
        ExternalRestTemplateBuilderFactory externalRestTemplateBuilderFactory = new ExternalRestTemplateBuilderFactory();
        externalRestTemplateBuilderFactory.requestFactory(() -> {
            SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
            requestFactory.setOutputStreaming(false);
            return requestFactory;
        });
        return externalRestTemplateBuilderFactory;
    }
}
