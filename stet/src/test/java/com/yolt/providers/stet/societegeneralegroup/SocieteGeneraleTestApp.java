package com.yolt.providers.stet.societegeneralegroup;

import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.rest.ExternalRestTemplateBuilderFactory;
import com.yolt.providers.common.rest.YoltProxySelectorBuilder;
import com.yolt.providers.common.util.SimpleRestTemplateManagerMock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.MockClock;
import io.micrometer.core.instrument.simple.SimpleConfig;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.contract.wiremock.WireMockConfigurationCustomizer;
import org.springframework.context.annotation.Bean;

import java.security.Security;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

@SpringBootApplication(scanBasePackages = {"com.yolt.providers.stet.generic.config",
        "com.yolt.providers.stet.societegeneralegroup"})
public class SocieteGeneraleTestApp {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Bean
    YoltProxySelectorBuilder getYoltProxySelectorBuilder() {
        return new YoltProxySelectorBuilder("Dummy", 0);
    }

    @Bean
    ExternalRestTemplateBuilderFactory getExternalRestTemplateBuilderFactory() {
        return new ExternalRestTemplateBuilderFactory();
    }

    @Bean
    Clock clock() {
        return Clock.fixed(Instant.parse("2021-12-31T00:00:00.123123Z"), ZoneOffset.UTC);
    }

    @Bean
    MeterRegistry mockedMeterRegistry() {
        return new SimpleMeterRegistry(SimpleConfig.DEFAULT,  new MockClock());
    }

    @Bean
    WireMockConfigurationCustomizer optionsCustomizer() {
        return options -> options
                .extensions(new ResponseTemplateTransformer(true))
                .keystoreType("pkcs12")
                .keystorePath("src/test/resources/certificates/fake-keystore.p12")
                .keystorePassword("changeit")
                .keyManagerPassword("changeit")
                .trustStorePassword("changeit");
    }

    @Bean
    RestTemplateManager restTemplateManager() {
        return new SimpleRestTemplateManagerMock();
    }
}
