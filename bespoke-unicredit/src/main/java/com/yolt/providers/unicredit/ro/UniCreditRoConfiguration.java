package com.yolt.providers.unicredit.ro;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.unicredit.common.ais.UniCreditDataProvider;
import com.yolt.providers.unicredit.common.config.UniCreditRegistrationProperties;
import com.yolt.providers.unicredit.common.data.UniCreditFetchDataService;
import com.yolt.providers.unicredit.common.data.UniCreditFetchDataServiceV2;
import com.yolt.providers.unicredit.common.data.mapper.*;
import com.yolt.providers.unicredit.common.data.transformer.ProviderStateJsonTransformer;
import com.yolt.providers.unicredit.common.rest.UniCreditHttpClientFactory;
import com.yolt.providers.unicredit.common.rest.UniCreditHttpHeadersProducerV1;
import com.yolt.providers.unicredit.common.service.UniCreditAuthenticationMeansProducerV1;
import com.yolt.providers.unicredit.common.service.UniCreditAuthorizationService;
import com.yolt.providers.unicredit.common.service.UniCreditAutoOnboardingService;
import com.yolt.providers.unicredit.common.service.UniCreditAutoOnboardingServiceV1;
import com.yolt.providers.unicredit.common.util.ProviderInfo;
import com.yolt.providers.unicredit.ro.service.UniCreditROAuthorizationService;
import io.micrometer.core.instrument.MeterRegistry;
import nl.ing.lovebird.extendeddata.account.BalanceType;
import nl.ing.lovebird.extendeddata.account.ExternalCashAccountType;
import nl.ing.lovebird.providerdomain.AccountType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Map;

import static com.yolt.providers.common.versioning.ProviderVersion.VERSION_1;

@Configuration
public class UniCreditRoConfiguration {

    @Bean("UniCreditRoDataProviderV1")
    public UniCreditDataProvider getUniCreditRoDataProviderV1(final MeterRegistry meterRegistry,
                                                              final UniCreditRoProperties properties,
                                                              @Qualifier("Unicredit") final ObjectMapper objectMapper,
                                                              final Clock clock,
                                                              final UniCreditRegistrationProperties registrationProperties) {
        UniCreditHttpClientFactory httpClientFactory = createHttpClientFactory(meterRegistry, objectMapper);
        UniCreditAuthMeansMapperV1 authMeansMapper = new UniCreditAuthMeansMapperV1();
        ProviderStateJsonTransformer stateTransformer = new ProviderStateJsonTransformer(objectMapper);
        return new UniCreditDataProvider(
                new UniCreditAuthenticationMeansProducerV1(),
                createAutoOnboardingService(httpClientFactory, authMeansMapper, registrationProperties),
                createAuthorizationService(properties, httpClientFactory, authMeansMapper, stateTransformer, clock),
                createFetchDataService(properties, httpClientFactory, authMeansMapper, stateTransformer, clock),
                new ProviderInfo("UNICREDIT_RO", "UniCredit (RO)", VERSION_1)
        );
    }

    private UniCreditFetchDataService createFetchDataService(final UniCreditRoProperties properties,
                                                             final UniCreditHttpClientFactory httpClientFactory,
                                                             final UniCreditAuthMeansMapperV1 authMeansMapper,
                                                             final ProviderStateJsonTransformer stateTransformer,
                                                             final Clock clock) {
        CurrencyCodeMapperV1 currencyCodeMapper = new CurrencyCodeMapperV1();
        Map<String, AccountType> supportedAccountTypes = Collections.singletonMap(ExternalCashAccountType.CURRENT.getCode(), AccountType.CURRENT_ACCOUNT);
        ZoneId timeZoneId = ZoneId.of("Europe/Bucharest");
        return new UniCreditFetchDataServiceV2(authMeansMapper,
                stateTransformer,
                httpClientFactory,
                properties,
                new UniCreditDataMapperV3(
                        currencyCodeMapper,
                        new BalanceMapperV1(
                                currencyCodeMapper,
                                timeZoneId),
                        new TransactionMapperV1(
                                currencyCodeMapper,
                                timeZoneId),
                        clock,
                        BalanceType.EXPECTED,
                        BalanceType.INTERIM_BOOKED,
                        supportedAccountTypes),
                timeZoneId);
    }

    private UniCreditAutoOnboardingService createAutoOnboardingService(final UniCreditHttpClientFactory httpClientFactory,
                                                                       final UniCreditAuthMeansMapperV1 authMeansMapper,
                                                                       final UniCreditRegistrationProperties registrationProperties) {
        return new UniCreditAutoOnboardingServiceV1(httpClientFactory, authMeansMapper, registrationProperties);
    }

    private UniCreditAuthorizationService createAuthorizationService(final UniCreditRoProperties properties,
                                                                     final UniCreditHttpClientFactory httpClientFactory,
                                                                     final UniCreditAuthMeansMapperV1 authMeansMapper,
                                                                     final ProviderStateJsonTransformer stateTransformer,
                                                                     final Clock clock) {
        return new UniCreditROAuthorizationService(
                httpClientFactory,
                authMeansMapper,
                properties,
                stateTransformer,
                clock
        );
    }

    private UniCreditHttpClientFactory createHttpClientFactory(final MeterRegistry meterRegistry,
                                                               final ObjectMapper objectMapper) {
        return new UniCreditHttpClientFactory(meterRegistry, objectMapper, new UniCreditHttpHeadersProducerV1());
    }
}
