package com.yolt.providers.volksbank;

import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.cloud.contract.wiremock.WireMockConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.Security;
import java.time.Clock;

/**
 * Test configuration. Solely to make some integrationstest 'easier to make'.
 * This is created when splitting of the providers. A lot of providers made use of spring of the application where they were actually used
 * to do some autowiring in integrationtests.
 */
@Configuration
public class VolksbankTestConfiguration {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    WireMockConfigurationCustomizer optionsCustomizer() {
        return options -> options
                .extensions(new ResponseTemplateTransformer(false))
                .keystoreType("pkcs12")
                .keystorePath("src/test/resources/certificates/volksbank/fake-keystore.p12")
                .keystorePassword("changeit")
                .keyManagerPassword("changeit");
    }
}
