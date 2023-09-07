package com.yolt.providers.starlingbank;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.rest.ExternalRestTemplateBuilderFactory;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.MockClock;
import io.micrometer.core.instrument.simple.SimpleConfig;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.Clock;

@SpringBootApplication(scanBasePackages = "com.yolt.providers.starlingbank")
public class TestApp {

    @Bean
    RestTemplateManager restTemplateManagerMock() {
        return new RestTemplateManagerMock(new ExternalRestTemplateBuilderFactory());
    }

    @Bean
    TestSigner testSigner() {
        return new TestSigner();
    }

    @Bean
    public Clock testClock() {
        return Clock.systemDefaultZone();
    }

    @Bean
    MeterRegistry mockedMeterRegistry() {
        return new SimpleMeterRegistry(SimpleConfig.DEFAULT,  new MockClock());
    }
}
