package com.yolt.providers.fineco;

import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.MockClock;
import io.micrometer.core.instrument.simple.SimpleConfig;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.cloud.contract.wiremock.WireMockConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.Security;

/**
 * Test configuration. Solely to make some integrationstest 'easier to make'.
 * This is created when splitting of the providers. A lot of providers made use of spring of the application where they were actually used
 * to do some autowiring in integrationtests.
 */
@Configuration
public class FinecoTestConfiguration {

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
    MeterRegistry mockedMeterRegistry() {
        return new SimpleMeterRegistry(SimpleConfig.DEFAULT,  new MockClock());
    }
}
