package com.yolt.providers.direkt1822group.direkt1822.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.direkt1822group.common.Direkt1822GroupDataProvider;
import com.yolt.providers.direkt1822group.common.rest.Direkt1822RestTemplateService;
import com.yolt.providers.direkt1822group.common.service.Direkt1822GroupAuthenticationService;
import com.yolt.providers.direkt1822group.common.service.Direkt1822GroupFetchDataService;
import com.yolt.providers.direkt1822group.common.service.Direkt1822GroupMapperService;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class Direkt1822BeanConfig {

    @Bean("Direkt1822DataProvider")
    public Direkt1822GroupDataProvider direkt1822DataProviderV1(MeterRegistry registry,
                                                                @Qualifier("Direkt1822GroupObjectMapper") ObjectMapper objectMapper,
                                                                Direkt1822Properties properties,
                                                                Clock clock) {
        return new Direkt1822GroupDataProvider(
                new Direkt1822GroupAuthenticationService(
                        new Direkt1822RestTemplateService(registry, objectMapper, properties),
                        clock
                ),
                new Direkt1822GroupFetchDataService(
                        new Direkt1822RestTemplateService(registry, objectMapper, properties),
                        new Direkt1822GroupMapperService(clock),
                        clock
                ),
                objectMapper,
                "DIREKT1822",
                "1822Direkt",
                ProviderVersion.VERSION_1,
                clock
        );
    }
}
