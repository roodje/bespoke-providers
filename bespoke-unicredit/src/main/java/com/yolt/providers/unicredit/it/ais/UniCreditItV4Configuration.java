package com.yolt.providers.unicredit.it.ais;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.unicredit.common.ais.UniCreditDataProvider;
import com.yolt.providers.unicredit.common.config.UniCreditRegistrationProperties;
import com.yolt.providers.unicredit.common.data.UniCreditFetchDataService;
import com.yolt.providers.unicredit.common.data.UniCreditFetchDataServiceV1;
import com.yolt.providers.unicredit.common.data.mapper.CurrencyCodeMapperV1;
import com.yolt.providers.unicredit.common.data.mapper.UniCreditAuthMeansMapperV1;
import com.yolt.providers.unicredit.common.data.mapper.UniCreditDataMapperV2;
import com.yolt.providers.unicredit.common.data.transformer.ProviderStateJsonTransformer;
import com.yolt.providers.unicredit.common.rest.UniCreditHttpClientFactory;
import com.yolt.providers.unicredit.common.rest.UniCreditHttpHeadersProducerV1;
import com.yolt.providers.unicredit.common.service.*;
import com.yolt.providers.unicredit.common.util.ProviderInfo;
import com.yolt.providers.unicredit.it.UniCreditItProperties;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

import static com.yolt.providers.common.versioning.ProviderVersion.VERSION_4;

@Configuration
public class UniCreditItV4Configuration {

    @Bean("UniCreditDataProviderV4")
    public UniCreditDataProvider getUniCreditDataProviderV4(final MeterRegistry meterRegistry,
                                                            final UniCreditItProperties properties,
                                                            @Qualifier("Unicredit") final ObjectMapper objectMapper,
                                                            final Clock clock,
                                                            final UniCreditRegistrationProperties registrationProperties) {
        UniCreditHttpClientFactory uniCreditHttpClientFactory = createHttpClientFactory(meterRegistry, objectMapper);
        ProviderStateJsonTransformer stateTransformer = new ProviderStateJsonTransformer(objectMapper);
        UniCreditAuthMeansMapperV1 authMeansMapper = new UniCreditAuthMeansMapperV1();
        return new UniCreditDataProvider(new UniCreditAuthenticationMeansProducerV1(),
                createAutoOnboardingService(uniCreditHttpClientFactory, authMeansMapper, registrationProperties),
                createAuthorizationService(properties, uniCreditHttpClientFactory, stateTransformer, authMeansMapper, clock),
                createFetchDataService(properties, uniCreditHttpClientFactory, stateTransformer, authMeansMapper, clock),
                new ProviderInfo("UNICREDIT", "Unicredit", VERSION_4));
    }

    private UniCreditFetchDataService createFetchDataService(final UniCreditItProperties properties,
                                                             final UniCreditHttpClientFactory uniCreditHttpClientFactory,
                                                             final ProviderStateJsonTransformer stateTransformer,
                                                             final UniCreditAuthMeansMapperV1 authMeansMapper,
                                                             final Clock clock) {
        return new UniCreditFetchDataServiceV1(uniCreditHttpClientFactory, stateTransformer, properties, new UniCreditDataMapperV2(new CurrencyCodeMapperV1(), clock), authMeansMapper);
    }

    private UniCreditAuthorizationService createAuthorizationService(UniCreditItProperties properties, UniCreditHttpClientFactory uniCreditHttpClientFactory, ProviderStateJsonTransformer stateTransformer, UniCreditAuthMeansMapperV1 authMeansMapper, final Clock clock) {
        return new UniCreditAuthorizationServiceV1(uniCreditHttpClientFactory, properties, stateTransformer, authMeansMapper, ZoneId.of("Europe/Rome"), clock);
    }

    private UniCreditAutoOnboardingService createAutoOnboardingService(final UniCreditHttpClientFactory uniCreditHttpClientFactory,
                                                                       final UniCreditAuthMeansMapperV1 authMeansMapper,
                                                                       final UniCreditRegistrationProperties registrationProperties) {
        return new UniCreditAutoOnboardingServiceV1(uniCreditHttpClientFactory, authMeansMapper, registrationProperties);
    }

    private UniCreditHttpClientFactory createHttpClientFactory(final MeterRegistry meterRegistry,
                                                               final ObjectMapper objectMapper) {
        return new UniCreditHttpClientFactory(meterRegistry, objectMapper, new UniCreditHttpHeadersProducerV1());
    }
}
