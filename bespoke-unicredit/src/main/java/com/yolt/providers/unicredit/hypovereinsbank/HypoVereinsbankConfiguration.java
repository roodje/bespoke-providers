package com.yolt.providers.unicredit.hypovereinsbank;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.unicredit.common.ais.UniCreditDataProvider;
import com.yolt.providers.unicredit.common.config.UniCreditBaseProperties;
import com.yolt.providers.unicredit.common.config.UniCreditRegistrationProperties;
import com.yolt.providers.unicredit.common.data.UniCreditFetchDataService;
import com.yolt.providers.unicredit.common.data.UniCreditFetchDataServiceV2;
import com.yolt.providers.unicredit.common.data.mapper.CurrencyCodeMapper;
import com.yolt.providers.unicredit.common.data.mapper.CurrencyCodeMapperV1;
import com.yolt.providers.unicredit.common.data.mapper.UniCreditAuthMeansMapper;
import com.yolt.providers.unicredit.common.data.mapper.UniCreditAuthMeansMapperV1;
import com.yolt.providers.unicredit.common.data.transformer.ProviderStateJsonTransformer;
import com.yolt.providers.unicredit.common.data.transformer.ProviderStateTransformer;
import com.yolt.providers.unicredit.common.dto.UniCreditAccessMeansDTO;
import com.yolt.providers.unicredit.common.rest.UniCreditHttpClientFactory;
import com.yolt.providers.unicredit.common.rest.UniCreditHttpHeadersProducerV1;
import com.yolt.providers.unicredit.common.service.*;
import com.yolt.providers.unicredit.common.util.ProviderInfo;
import com.yolt.providers.unicredit.hypovereinsbank.data.mapper.HypoVereinsbankBalanceMapper;
import com.yolt.providers.unicredit.hypovereinsbank.data.mapper.HypoVereinsbankDataMapper;
import com.yolt.providers.unicredit.hypovereinsbank.data.mapper.HypoVereinsbankTransactionMapper;
import io.micrometer.core.instrument.MeterRegistry;
import nl.ing.lovebird.extendeddata.account.BalanceType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class HypoVereinsbankConfiguration {

    @Bean("HypoVereinsbankDataProvider")
    public UniCreditDataProvider getHypoVereinsbankDataProvider(final MeterRegistry meterRegistry,
                                                                @Qualifier("Unicredit") final ObjectMapper objectMapper,
                                                                final Clock clock,
                                                                final UniCreditRegistrationProperties registrationProperties,
                                                                final HypoVereinsbankProperties properties) {
        UniCreditHttpClientFactory httpClientFactory = new UniCreditHttpClientFactory(meterRegistry, objectMapper, new UniCreditHttpHeadersProducerV1());
        UniCreditAuthMeansMapperV1 authMeansMapper = new UniCreditAuthMeansMapperV1();
        ProviderStateJsonTransformer stateTransformer = new ProviderStateJsonTransformer(objectMapper);
        ZoneId berlinZoneId = ZoneId.of("Europe/Berlin");
        CurrencyCodeMapperV1 currencyCodeMapper = new CurrencyCodeMapperV1();
        return new UniCreditDataProvider(
                new UniCreditAuthenticationMeansProducerV1(),
                createAutoOnboardingService(registrationProperties, httpClientFactory, authMeansMapper),
                createAuthorizationService(properties, httpClientFactory, authMeansMapper, stateTransformer, berlinZoneId, clock),
                createFetchDataService(properties, httpClientFactory, authMeansMapper, stateTransformer, berlinZoneId, clock, currencyCodeMapper),
                new ProviderInfo("HYPOVEREINSBANK", "HypoVereinsbank (UniCredit)", ProviderVersion.VERSION_1)
        );
    }

    private UniCreditFetchDataService createFetchDataService(final UniCreditBaseProperties properties,
                                                             final UniCreditHttpClientFactory httpClientFactory,
                                                             final UniCreditAuthMeansMapper authMeansMapper,
                                                             final ProviderStateTransformer<UniCreditAccessMeansDTO> stateTransformer,
                                                             final ZoneId berlinZoneId,
                                                             final Clock clock,
                                                             final CurrencyCodeMapper currencyCodeMapper) {
        return new UniCreditFetchDataServiceV2(
                authMeansMapper,
                stateTransformer,
                httpClientFactory,
                properties,
                new HypoVereinsbankDataMapper(
                        currencyCodeMapper,
                        new HypoVereinsbankBalanceMapper(currencyCodeMapper, berlinZoneId),
                        new HypoVereinsbankTransactionMapper(currencyCodeMapper, berlinZoneId),
                        clock,
                        BalanceType.EXPECTED,
                        BalanceType.CLOSING_BOOKED
                ),
                berlinZoneId
        );
    }

    private UniCreditAutoOnboardingService createAutoOnboardingService(final UniCreditRegistrationProperties registrationProperties,
                                                                       final UniCreditHttpClientFactory httpClientFactory,
                                                                       final UniCreditAuthMeansMapper authMeansMapper) {
        return new UniCreditAutoOnboardingServiceV1(httpClientFactory,
                authMeansMapper,
                registrationProperties);
    }

    private UniCreditAuthorizationService createAuthorizationService(final UniCreditBaseProperties properties,
                                                                     final UniCreditHttpClientFactory httpClientFactory,
                                                                     final UniCreditAuthMeansMapper authMeansMapper,
                                                                     final ProviderStateTransformer<UniCreditAccessMeansDTO> stateTransformer,
                                                                     final ZoneId berlinZoneId,
                                                                     final Clock clock) {
        return new UniCreditAuthorizationServiceV1(
                httpClientFactory,
                properties,
                stateTransformer,
                authMeansMapper,
                berlinZoneId,
                clock);
    }
}
