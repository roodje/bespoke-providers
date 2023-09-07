package com.yolt.providers.cbiglobe;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.rest.ExternalRestTemplateBuilderFactory;
import com.yolt.providers.common.rest.YoltProxySelectorBuilder;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.MockClock;
import io.micrometer.core.instrument.simple.SimpleConfig;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.security.Security;
import java.time.Clock;

@SpringBootApplication
@Import(CbiGlobeTestConfiguration.class)
public class CbiGlobeTestApp {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Bean
    Clock getClock() {
        return Clock.systemUTC();
    }

    @Bean
    MeterRegistry mockedMeterRegistry() {
        return new SimpleMeterRegistry(SimpleConfig.DEFAULT,  new MockClock());
    }

    @Bean
    YoltProxySelectorBuilder yoltProxySelectorBuilder() {
        return new YoltProxySelectorBuilder("Dummy", 0);
    }

    @Bean
    RestTemplateManager restTemplateManager() {
        ExternalRestTemplateBuilderFactory factory = new ExternalRestTemplateBuilderFactory();
        factory.requestFactory(SimpleClientHttpRequestFactory::new);
        return new RestTemplateManagerMock(factory);
    }
}
