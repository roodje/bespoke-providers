package com.yolt.providers.rabobank.config;

import com.yolt.providers.rabobank.RabobankAccountsAndTransactionsServiceV4;
import com.yolt.providers.rabobank.RabobankAuthenticationServiceV1;
import com.yolt.providers.rabobank.RabobankDataProvider;
import com.yolt.providers.rabobank.RabobankObjectMapperV3;
import com.yolt.providers.rabobank.http.RabobankAisHttpClientFactory;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

import static com.yolt.providers.common.versioning.ProviderVersion.VERSION_5;

@Configuration
public class RabobankBeanConfig {

    @Bean("RabobankDataProviderV5")
    public RabobankDataProvider getRabobankDataProviderV5(final RabobankProperties properties,
                                                          final MeterRegistry meterRegistry,
                                                          final Clock clock) {
        return new RabobankDataProvider(
                clock,
                properties,
                new RabobankAisHttpClientFactory(properties, meterRegistry, "RABOBANK", RabobankObjectMapperV3.OBJECT_MAPPER),
                new RabobankAuthenticationServiceV1(properties, clock),
                new RabobankAccountsAndTransactionsServiceV4(clock),
                VERSION_5
        );
    }
}
