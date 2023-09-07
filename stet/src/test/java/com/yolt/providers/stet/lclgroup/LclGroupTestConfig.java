package com.yolt.providers.stet.lclgroup;

import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.rest.YoltProxySelectorBuilder;
import com.yolt.providers.common.util.SimpleRestTemplateManagerMock;
import com.yolt.providers.stet.SignerMock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.MockClock;
import io.micrometer.core.instrument.simple.SimpleConfig;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.contract.wiremock.WireMockConfigurationCustomizer;
import org.springframework.context.annotation.Bean;

import java.time.Clock;

@SpringBootApplication(scanBasePackages = {
        "com.yolt.providers.stet.generic.config",
        "com.yolt.providers.stet.lclgroup.lcl"})
public class LclGroupTestConfig {

    @Bean
    YoltProxySelectorBuilder getYoltProxySelectorBuilder() {
        return new YoltProxySelectorBuilder("Dummy", 0);
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

    @Bean
    MeterRegistry mockedMeterRegistry() {
        return new SimpleMeterRegistry(SimpleConfig.DEFAULT,  new MockClock());
    }

    @Bean
    Signer signer() {
        return new SignerMock();
    }

    @Bean
    Clock getClock(){
        return Clock.systemUTC();
    }
}
