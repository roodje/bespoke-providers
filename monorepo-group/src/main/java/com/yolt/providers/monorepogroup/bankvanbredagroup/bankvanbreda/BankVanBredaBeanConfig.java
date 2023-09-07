package com.yolt.providers.monorepogroup.bankvanbredagroup.bankvanbreda;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.rest.http.DefaultHttpErrorHandlerV2;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.monorepogroup.bankvanbredagroup.BankVanBredaGroupDataProvider;
import com.yolt.providers.monorepogroup.bankvanbredagroup.common.ais.auth.service.BankVanBredaGroupAuthenticationService;
import com.yolt.providers.monorepogroup.bankvanbredagroup.common.ais.data.service.BankVanBredaDataMapper;
import com.yolt.providers.monorepogroup.bankvanbredagroup.common.ais.data.service.BankVanBredaFetchDataService;
import com.yolt.providers.monorepogroup.bankvanbredagroup.common.ais.service.BankVanBredaGroupHttpClientFactory;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

import static com.yolt.providers.monorepogroup.bankvanbredagroup.bankvanbreda.BankVanBredaDetailsProvider.BANK_VAN_BREDA_PROVIDER_KEY;
import static com.yolt.providers.monorepogroup.bankvanbredagroup.bankvanbreda.BankVanBredaDetailsProvider.BANK_VAN_BREDA_PROVIDER_NAME;

@Configuration
public class BankVanBredaBeanConfig {

    @Bean("BankVanBredaDataProviderV1")
    public BankVanBredaGroupDataProvider getBankVanBredaDataProviderV1(MeterRegistry meterRegistry,
                                                                       BankVanBredaProperties properties,
                                                                       @Qualifier("BankVanBredaGroupObjectMapper") ObjectMapper objectMapper,
                                                                       Clock clock) {
        BankVanBredaGroupHttpClientFactory httpClientFactory =
                new BankVanBredaGroupHttpClientFactory(
                        meterRegistry,
                        BANK_VAN_BREDA_PROVIDER_KEY,
                        objectMapper,
                        clock,
                        properties,
                        new DefaultHttpErrorHandlerV2());
        BankVanBredaGroupAuthenticationService authenticationService =
                new BankVanBredaGroupAuthenticationService(
                        httpClientFactory,
                        BANK_VAN_BREDA_PROVIDER_KEY,
                        clock);
        BankVanBredaFetchDataService fetchDataService = new BankVanBredaFetchDataService(httpClientFactory,
                new BankVanBredaDataMapper(clock),
                properties);
        return new BankVanBredaGroupDataProvider(
                BANK_VAN_BREDA_PROVIDER_KEY,
                BANK_VAN_BREDA_PROVIDER_NAME,
                ProviderVersion.VERSION_1,
                authenticationService,
                fetchDataService,
                objectMapper,
                clock);
    }
}
