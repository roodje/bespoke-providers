package com.yolt.providers.volksbank.asn.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.volksbank.common.ais.VolksbankDataProviderV4;
import com.yolt.providers.volksbank.common.config.ProviderIdentification;
import com.yolt.providers.volksbank.common.rest.VolksbankHttpClientFactoryV2;
import com.yolt.providers.volksbank.common.rest.VolksbankHttpErrorHandler;
import com.yolt.providers.volksbank.common.service.VolksbankAuthorizationServiceV4;
import com.yolt.providers.volksbank.common.service.VolksbankFetchDataServiceV4;
import com.yolt.providers.volksbank.common.service.mapper.CurrencyCodeMapper;
import com.yolt.providers.volksbank.common.service.mapper.CurrencyCodeMapperV1;
import com.yolt.providers.volksbank.common.service.mapper.VolksbankDataMapperServiceV1;
import com.yolt.providers.volksbank.common.service.mapper.VolksbankExtendedDataMapperV1;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

import static com.yolt.providers.common.versioning.ProviderVersion.VERSION_5;

@Configuration
public class ASNBeanConfigV2 {

    private static final String IDENTIFIER = "ASN_BANK";
    private static final String DISPLAY_NAME = "ASN Bank";

    @Bean("ASNDataProviderV5")
    public VolksbankDataProviderV4 getASNDataProviderV5(@Qualifier("Volksbank") final ObjectMapper objectMapper,
                                                        final ASNProperties properties,
                                                        final MeterRegistry meterRegistry,
                                                        final Clock clock) {
        ProviderIdentification providerIdentification = new ProviderIdentification(IDENTIFIER, DISPLAY_NAME, VERSION_5);

        VolksbankAuthorizationServiceV4 authorizationService = getAuthorizationServiceV4(clock);
        VolksbankFetchDataServiceV4 fetchDataService = getFetchDataServiceV4(properties, clock);
        VolksbankHttpClientFactoryV2 httpClientFactory = getHttpClientFactoryV2(objectMapper, properties, meterRegistry);

        return new VolksbankDataProviderV4(properties,
                authorizationService,
                fetchDataService,
                httpClientFactory,
                providerIdentification,
                objectMapper,
                clock);
    }

    public VolksbankAuthorizationServiceV4 getAuthorizationServiceV4(final Clock clock) {
        return new VolksbankAuthorizationServiceV4(clock);
    }

    public VolksbankFetchDataServiceV4 getFetchDataServiceV4(final ASNProperties properties, final Clock clock) {
        CurrencyCodeMapper currencyCodeMapper = new CurrencyCodeMapperV1();
        return new VolksbankFetchDataServiceV4(properties,
                new VolksbankDataMapperServiceV1(new VolksbankExtendedDataMapperV1(currencyCodeMapper), currencyCodeMapper, clock));
    }

    public VolksbankHttpClientFactoryV2 getHttpClientFactoryV2(final ObjectMapper objectMapper,
                                                               final ASNProperties properties,
                                                               final MeterRegistry meterRegistry) {
        return new VolksbankHttpClientFactoryV2(objectMapper, meterRegistry, new VolksbankHttpErrorHandler(), properties);
    }
}
