package com.yolt.providers.monorepogroup.sebaggroup.sebag;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.monorepogroup.sebaggroup.common.SebAgGroupDataProvider;
import com.yolt.providers.monorepogroup.sebaggroup.common.config.dto.internal.ProviderIdentification;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class SebAgBeanConfig {

    public static final String PROVIDER_IDENTIFIER = "SEB_AG";
    public static final String PROVIDER_DISPLAY_NAME = "SEB AG";

    @Bean("SebAgDataProviderV1")
    UrlDataProvider getSebAgDataProviderV1(SebAgProperties properties,
                                           MeterRegistry meterRegistry,
                                           Clock clock,
                                           @Qualifier("SebAgGroupObjectMapper") ObjectMapper objectMapper) {
        return new SebAgGroupDataProvider(
                new ProviderIdentification(PROVIDER_IDENTIFIER, PROVIDER_DISPLAY_NAME, ProviderVersion.VERSION_1)
        );
    }
}
