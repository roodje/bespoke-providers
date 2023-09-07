package com.yolt.providers.raiffeisenbank.ro.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.rest.http.DefaultHttpErrorHandlerV2;
import com.yolt.providers.raiffeisenbank.common.RaiffeisenBankHttpClientFactory;
import com.yolt.providers.raiffeisenbank.common.ais.RaiffeisenBankDataProviderV1;
import com.yolt.providers.raiffeisenbank.common.ais.auth.service.RaiffeisenBankAuthenticationService;
import com.yolt.providers.raiffeisenbank.common.ais.config.ProviderIdentification;
import com.yolt.providers.raiffeisenbank.common.ais.data.service.RaiffeisenBankFetchDataService;
import com.yolt.providers.raiffeisenbank.common.ais.data.service.RaiffeisenDataMapper;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

import static com.yolt.providers.common.versioning.ProviderVersion.VERSION_1;

@Configuration
public class RaiffeisenBankRoBeanConfigV1 {

    public static final String IDENTIFIER = "RAIFFEISEN_BANK_RO";
    public static final String DISPLAY_NAME = "Raiffeisen Bank";
    private static final ZoneId ZONE_ID = ZoneId.of(("Europe/Bucharest"));

    @Bean("RaiffeisenBankRoDataProviderV1")
    public RaiffeisenBankDataProviderV1 getRaiffeisenBankRoDataProviderV1(MeterRegistry meterRegistry,
                                                                          RaiffeisenBankRoProperties properties,
                                                                          @Qualifier("RaiffeisenBankObjectMapper") ObjectMapper objectMapper,
                                                                          Clock clock) {
        ProviderIdentification providerIdentification = new ProviderIdentification(IDENTIFIER, DISPLAY_NAME, VERSION_1);
        RaiffeisenBankHttpClientFactory httpClientFactory = new RaiffeisenBankHttpClientFactory(
                meterRegistry,
                IDENTIFIER,
                objectMapper,
                properties,
                new DefaultHttpErrorHandlerV2(),
                clock);
        RaiffeisenBankAuthenticationService authenticationService = new RaiffeisenBankAuthenticationService(
                httpClientFactory,
                properties,
                clock
        );
        RaiffeisenDataMapper dataMapper = new RaiffeisenDataMapper(ZONE_ID, clock);
        RaiffeisenBankFetchDataService fetchDataService = new RaiffeisenBankFetchDataService(
                httpClientFactory,
                properties,
                dataMapper);
        return new RaiffeisenBankDataProviderV1(
                providerIdentification,
                authenticationService,
                fetchDataService,
                objectMapper,
                clock
        );
    }
}
