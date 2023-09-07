package com.yolt.providers.bunq;

import com.yolt.providers.common.rest.ExternalRestTemplateBuilderFactory;
import com.yolt.providers.common.rest.YoltProxySelectorBuilder;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.MockClock;
import io.micrometer.core.instrument.simple.SimpleConfig;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

@SpringBootApplication(scanBasePackages = "com.yolt.providers.bunq")
public class TestApp {

    @Bean
    YoltProxySelectorBuilder getYoltProxySelectorBuilder() {
        return new YoltProxySelectorBuilder("Dummy", 0);
    }

    @Bean
    ExternalRestTemplateBuilderFactory getExternalRestTemplateBuilderFactory() {
        return new ExternalRestTemplateBuilderFactory();
    }

    @Bean
    MeterRegistry mockedMeterRegistry() {
        return new SimpleMeterRegistry(SimpleConfig.DEFAULT, new MockClock());
    }

    @Bean
    @Primary
    Clock clockworkOrange() {
        return Clock.fixed(Instant.parse("2019-01-01T00:00:00Z"), ZoneId.of("Europe/Amsterdam"));
    }

}
