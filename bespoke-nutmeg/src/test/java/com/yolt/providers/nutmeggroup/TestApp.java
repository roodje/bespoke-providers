package com.yolt.providers.nutmeggroup;

import com.yolt.providers.common.rest.ExternalRestTemplateBuilderFactory;
import com.yolt.providers.common.rest.YoltProxySelectorBuilder;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.time.Clock;

// This class is only used to setup Spring context for integration tests in multi module project
@SpringBootApplication
@Import(TestConfiguration.class)
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
    Clock getClock(){
        return Clock.systemUTC();
    }
}
