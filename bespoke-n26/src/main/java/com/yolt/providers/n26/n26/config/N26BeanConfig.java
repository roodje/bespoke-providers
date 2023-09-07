package com.yolt.providers.n26.n26.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.n26.common.auth.N26GroupAuthenticationMeansProducerV1;
import com.yolt.providers.n26.common.auth.N26GroupPKCE;
import com.yolt.providers.n26.common.consentwindow.ThreeMonthsAfterFifteenMinutesConsentWindow;
import com.yolt.providers.n26.common.http.N26GroupHttpClientFactory;
import com.yolt.providers.n26.common.service.ConsentStatusPoller;
import com.yolt.providers.n26.common.service.N26GroupAuthorizationService;
import com.yolt.providers.n26.common.service.N26GroupFetchDataService;
import com.yolt.providers.n26.common.service.PollingDelayCalculationWithInitialDelayStrategy;
import com.yolt.providers.n26.common.service.mapper.N26GroupAccountMapper;
import com.yolt.providers.n26.common.service.mapper.N26GroupProviderStateMapper;
import com.yolt.providers.n26.common.service.mapper.N26GroupTransactionMapper;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.util.concurrent.Executors;

@Configuration
public class N26BeanConfig {

    @Bean("N26HttpClientFactory")
    public N26GroupHttpClientFactory getN26HttpClientFactory(MeterRegistry meterRegistry,
                                                             N26Properties properties,
                                                             @Qualifier("N26GroupObjectMapper") ObjectMapper objectMapper) {
        return new N26GroupHttpClientFactory(objectMapper, meterRegistry, properties);
    }

    @Bean("N26AuthenticationMeansProducerV1")
    public N26GroupAuthenticationMeansProducerV1 getN26AuthenticationMeansProducerV1() {
        return new N26GroupAuthenticationMeansProducerV1();
    }

    @Bean("N26AuthorizationServiceV1")
    public N26GroupAuthorizationService getN26AuthorizationServiceV1(N26Properties properties,
                                                                     @Qualifier("N26GroupObjectMapper") ObjectMapper objectMapper,
                                                                     Clock clock) {
        return new N26GroupAuthorizationService(new N26GroupProviderStateMapper(objectMapper, clock),
                new N26GroupPKCE(),
                properties,
                new ConsentStatusPoller(Executors.newScheduledThreadPool(properties.getCorePoolSize()),
                        new PollingDelayCalculationWithInitialDelayStrategy(properties.getConsentStatusPollingInitialDelaySeconds()),
                        properties.getConsentStatusPollingTotalDelayLimitSeconds()),
                clock);
    }

    @Bean("N26FetchDataServiceV1")
    public N26GroupFetchDataService getN26FetchDataServiceV1(N26Properties properties,
                                                             @Qualifier("N26GroupObjectMapper") ObjectMapper objectMapper,
                                                             Clock clock) {
        return new N26GroupFetchDataService(
                new N26GroupAccountMapper(clock),
                new N26GroupTransactionMapper(),
                new N26GroupProviderStateMapper(objectMapper, clock),
                new ThreeMonthsAfterFifteenMinutesConsentWindow(),
                properties,
                clock);
    }
}
