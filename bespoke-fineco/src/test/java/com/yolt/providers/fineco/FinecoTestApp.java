package com.yolt.providers.fineco;

import com.yolt.providers.common.rest.ExternalRestTemplateBuilderFactory;
import com.yolt.providers.common.rest.YoltProxySelectorBuilder;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.time.Clock;

@SpringBootApplication
@Import(FinecoTestConfiguration.class)
public class FinecoTestApp {

    @Bean
    Clock getClock() {
        return Clock.systemUTC();
    }

    @Bean
    YoltProxySelectorBuilder getYoltProxySelectorBuilder() {
        return new YoltProxySelectorBuilder("Dummy", 0);
    }

    @Bean
    ExternalRestTemplateBuilderFactory getExternalRestTemplateBuilderFactory() {
        ExternalRestTemplateBuilderFactory externalRestTemplateBuilderFactory = new ExternalRestTemplateBuilderFactory();
        externalRestTemplateBuilderFactory.requestFactory(SimpleClientHttpRequestFactory::new);
        return externalRestTemplateBuilderFactory;
    }
}
