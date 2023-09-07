package com.yolt.providers.abnamrogroup;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.yolt.providers.common.rest.ExternalRestTemplateBuilderFactory;
import com.yolt.providers.common.rest.YoltProxySelectorBuilder;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.MockClock;
import io.micrometer.core.instrument.simple.SimpleConfig;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;

import java.time.Clock;

@SpringBootApplication(scanBasePackages = "com.yolt.providers.abnamrogroup")
public class TestApp {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    YoltProxySelectorBuilder getYoltProxySelectorBuilder() {
        return new YoltProxySelectorBuilder("Dummy", 0);
    }

    @Bean
    ExternalRestTemplateBuilderFactory getExternalRestTemplateBuilderFactory() {
        return new ExternalRestTemplateBuilderFactory();
    }

    // taken from lovebird commons
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonObjectMapperBuilderCustomizer() {
        return builder -> builder.featuresToDisable(
                // Prevent user data snippets ending up in the logs on JsonParseExceptions
                JsonParser.Feature.INCLUDE_SOURCE_IN_LOCATION,
                // Format Dates instead of returning a long
                SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,
                // Tolerate new fields
                DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                // Do not normalize time zone to UTC
                DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
                // Use Jackson 2.10 timezone format (RFC 822) rather then ISO 8601.
                .dateFormat(new StdDateFormat().withColonInTimeZone(false));
    }

    @Bean
    MeterRegistry mockedMeterRegistry() {
        return new SimpleMeterRegistry(SimpleConfig.DEFAULT,  new MockClock());
    }
}
