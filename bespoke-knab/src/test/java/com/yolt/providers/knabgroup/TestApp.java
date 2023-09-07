package com.yolt.providers.knabgroup;

import com.yolt.providers.common.rest.ExternalRestTemplateBuilderFactory;
import com.yolt.providers.common.rest.YoltProxySelectorBuilder;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.MockClock;
import io.micrometer.core.instrument.simple.SimpleConfig;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.time.Clock;

// This class is only used to setup Spring context for integration tests in multi module project
@SpringBootApplication
@Import(TestConfiguration.class)
public class TestApp {

    @Bean
    Clock getClock() {
        return Clock.systemUTC();
    }

    @Bean
    YoltProxySelectorBuilder getYoltProxySelectorBuilder() {
        return new YoltProxySelectorBuilder("Dummy", 0);
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

    @Bean
    MeterRegistry mockedMeterRegistry() {
        return new SimpleMeterRegistry(SimpleConfig.DEFAULT, new MockClock());
    }
}
