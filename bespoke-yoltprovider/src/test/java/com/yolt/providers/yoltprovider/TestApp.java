package com.yolt.providers.yoltprovider;

import com.yolt.providers.common.rest.ExternalRestTemplateBuilderFactory;
import com.yolt.providers.common.rest.YoltProxySelectorBuilder;
import io.opentracing.Tracer;
import io.opentracing.mock.MockTracer;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.Clock;

@SpringBootApplication
public class TestApp {

    @Bean
    Tracer tracer() {
        return new MockTracer();
    }

    @Bean
    ExternalRestTemplateBuilderFactory getExternalRestTemplateBuilder() {
        return new ExternalRestTemplateBuilderFactory();
    }

    @Bean
    YoltProxySelectorBuilder getYoltProxySelectorBuilder() {
        return new YoltProxySelectorBuilder("Dummy", 0);
    }

    @Bean
    public Clock testClock() {
        return Clock.systemDefaultZone();
    }
}