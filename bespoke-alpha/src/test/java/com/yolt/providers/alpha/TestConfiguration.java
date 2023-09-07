package com.yolt.providers.alpha;

import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.rest.ExternalRestTemplateBuilderFactory;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.MockClock;
import io.micrometer.core.instrument.simple.SimpleConfig;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import lombok.SneakyThrows;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.cloud.contract.wiremock.WireMockConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

@Configuration
public class TestConfiguration {

    @Bean
    Clock clock() {
        return Clock.fixed(Instant.parse("2022-01-01T00:00:00Z"), ZoneId.of("UTC"));
    }

    @Bean
    MeterRegistry mockedMeterRegistry() {
        return new SimpleMeterRegistry(SimpleConfig.DEFAULT, new MockClock());
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

    @SneakyThrows
    @Bean
    RestTemplateManager getRestTemplateManager() {
        CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLSocketFactory(new SSLConnectionSocketFactory(new SSLContextBuilder()
                        .loadTrustMaterial(null, new TrustSelfSignedStrategy())
                        .build()))
                .build();

        ExternalRestTemplateBuilderFactory externalRestTemplateBuilderFactory = new ExternalRestTemplateBuilderFactory();
        externalRestTemplateBuilderFactory.requestFactory(() -> new HttpComponentsClientHttpRequestFactory(httpClient));
        return new RestTemplateManagerMock(externalRestTemplateBuilderFactory);
    }
}

