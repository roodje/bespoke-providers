package com.yolt.providers.monorepogroup.libragroup.libra;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.rest.http.DefaultHttpErrorHandlerV3;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.monorepogroup.libragroup.common.LibraGroupDataProvider;
import com.yolt.providers.monorepogroup.libragroup.common.LibraGroupHttpClientFactoryV1;
import com.yolt.providers.monorepogroup.libragroup.common.LibraSigningServiceV1;
import com.yolt.providers.monorepogroup.libragroup.common.ais.auth.LibraGroupAuthenticationServiceV1;
import com.yolt.providers.monorepogroup.libragroup.common.ais.data.LibraDataMapperV1;
import com.yolt.providers.monorepogroup.libragroup.common.ais.data.LibraFetchDataServiceV1;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

import static com.yolt.providers.monorepogroup.libragroup.libra.LibraDetailsProvider.LIBRA_PROVIDER_KEY;
import static com.yolt.providers.monorepogroup.libragroup.libra.LibraDetailsProvider.LIBRA_PROVIDER_NAME;

@Configuration
public class LibraBeanConfig {

    @Bean("LibraDataProviderV1")
    public LibraGroupDataProvider getLibraDataProviderV1(MeterRegistry meterRegistry,
                                                         LibraProperties properties,
                                                         @Qualifier("LibraGroupObjectMapper") ObjectMapper objectMapper,
                                                         Clock clock) {

        LibraGroupHttpClientFactoryV1 httpClientFactory =
                new LibraGroupHttpClientFactoryV1(
                        meterRegistry,
                        LIBRA_PROVIDER_KEY,
                        objectMapper,
                        clock,
                        properties,
                        new DefaultHttpErrorHandlerV3(),
                        new LibraSigningServiceV1(objectMapper));
        LibraGroupAuthenticationServiceV1 authenticationService =
                new LibraGroupAuthenticationServiceV1(
                        properties,
                        httpClientFactory,
                        LIBRA_PROVIDER_KEY,
                        clock);
        LibraFetchDataServiceV1 libraFetchDataService =
                new LibraFetchDataServiceV1(
                        httpClientFactory,
                        new LibraDataMapperV1(clock));
        return new LibraGroupDataProvider(
                authenticationService,
                libraFetchDataService,
                objectMapper,
                clock,
                LIBRA_PROVIDER_KEY,
                LIBRA_PROVIDER_NAME,
                ProviderVersion.VERSION_1
        );
    }
}