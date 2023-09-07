package com.yolt.providers.abancagroup.abanca.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.abancagroup.common.AbancaHttpClientFactory;
import com.yolt.providers.abancagroup.common.ais.AbancaDataProviderV1;
import com.yolt.providers.abancagroup.common.ais.auth.service.AbancaAuthenticationService;
import com.yolt.providers.abancagroup.common.ais.config.ProviderIdentification;
import com.yolt.providers.abancagroup.common.ais.data.service.AbancaDataMapper;
import com.yolt.providers.abancagroup.common.ais.data.service.AbancaFetchDataService;
import com.yolt.providers.abancagroup.common.ais.data.service.AbancaSigningService;
import com.yolt.providers.common.rest.http.DefaultHttpErrorHandlerV2;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

import static com.yolt.providers.abancagroup.abanca.config.AbancaDetailsProvider.DISPLAY_NAME;
import static com.yolt.providers.abancagroup.abanca.config.AbancaDetailsProvider.IDENTIFIER;
import static com.yolt.providers.common.versioning.ProviderVersion.VERSION_1;

@Configuration
public class AbancaBeanConfigV1 {

    private static final ProviderIdentification PROVIDER_IDENTIFICATION =
            new ProviderIdentification(IDENTIFIER, DISPLAY_NAME, VERSION_1);

    @Bean("AbancaDataProviderV1")
    public AbancaDataProviderV1 getAbancaDataProviderV1(MeterRegistry meterRegistry,
                                                        AbancaProperties properties,
                                                        @Qualifier("AbancaGroupObjectMapper") ObjectMapper objectMapper,
                                                        Clock clock) {
        AbancaHttpClientFactory httpClientFactory = new AbancaHttpClientFactory(meterRegistry,
                IDENTIFIER,
                objectMapper,
                properties,
                new DefaultHttpErrorHandlerV2(),
                new AbancaSigningService());
        AbancaAuthenticationService authenticationService = new AbancaAuthenticationService(httpClientFactory,
                properties,
                clock);
        AbancaFetchDataService fetchDataService = new AbancaFetchDataService(httpClientFactory,
                new AbancaDataMapper(clock),
                properties,
                clock);
        return new AbancaDataProviderV1(PROVIDER_IDENTIFICATION,
                authenticationService,
                fetchDataService,
                objectMapper,
                clock);
    }
}
