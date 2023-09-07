package com.yolt.providers.alpha.alphabankromania;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.alpha.common.AlphaDataProvider;
import com.yolt.providers.alpha.common.auth.AlphaSigner;
import com.yolt.providers.alpha.common.auth.AlphaTypedAuthenticationMeansProducer;
import com.yolt.providers.alpha.common.auth.service.AlphaAuthenticationService;
import com.yolt.providers.alpha.common.config.AlphaKeyRequirementsProducer;
import com.yolt.providers.alpha.common.http.AlphaHttpClientFactory;
import com.yolt.providers.common.rest.http.DefaultHttpErrorHandlerV3;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

import static com.yolt.providers.common.versioning.ProviderVersion.VERSION_1;

@Configuration
public class AlphaBankRomaniaBeanConfig {

    @Bean("AlphaBankDataProviderV1")
    public AlphaDataProvider getAlphaDataProvider(AlphaBankRomaniaProperties properties,
                                                  MeterRegistry registry,
                                                  Clock clock) {
        ObjectMapper objectMapper = new ObjectMapper();
        return new AlphaDataProvider(
                "ALPHA_BANK_ROMANIA",
                "Alpha Bank Romania",
                VERSION_1,
                new AlphaTypedAuthenticationMeansProducer(),
                new AlphaKeyRequirementsProducer(),
                new AlphaHttpClientFactory(registry, properties, objectMapper, new DefaultHttpErrorHandlerV3()),
                new AlphaAuthenticationService(properties, objectMapper, new AlphaSigner(clock)),
                objectMapper,
                clock
        );
    }
}
