package com.yolt.providers.deutschebank.de;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.deutschebank.common.auth.DeutscheBankGroupAuthenticationMeansProducer;
import com.yolt.providers.deutschebank.common.auth.DeutscheBankGroupAuthenticationMeansProducerV1;
import com.yolt.providers.deutschebank.common.config.DeutscheBankGroupDateConverter;
import com.yolt.providers.deutschebank.common.http.DeutscheBankGroupHttpClientFactory;
import com.yolt.providers.deutschebank.common.mapper.DeutscheBankGroupAccountMapper;
import com.yolt.providers.deutschebank.common.mapper.DeutscheBankGroupProviderStateMapper;
import com.yolt.providers.deutschebank.common.mapper.DeutscheBankGroupTransactionMapper;
import com.yolt.providers.deutschebank.common.service.authorization.DeutscheBankGroupAuthorizationService;
import com.yolt.providers.deutschebank.common.service.authorization.consent.DeutscheBankGroupDrivenConsentRequestStrategy;
import com.yolt.providers.deutschebank.common.service.authorization.form.DeutscheBankGroupFKDNFormStrategy;
import com.yolt.providers.deutschebank.common.service.fetchdata.DeutscheBankGroupFetchDataService;
import com.yolt.providers.deutschebank.common.service.fetchdata.accounts.DeutscheBankGroupFetchAccountsWithoutBalancesStrategy;
import com.yolt.providers.deutschebank.common.service.fetchdata.transactions.DeutscheBankGroupFetchTransactionsBothStatusStrategy;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class DeutscheBankDataProviderBeanConfig {

    @Bean("DeutscheBankHttpClientFactory")
    public DeutscheBankGroupHttpClientFactory httpClientFactory(MeterRegistry meterRegistry,
                                                                DeutscheBankProperties properties,
                                                                @Qualifier("DeutscheBankGroupObjectMapper") ObjectMapper objectMapper) {
        return new DeutscheBankGroupHttpClientFactory(objectMapper, meterRegistry, properties);
    }

    @Bean("DeutscheBankAuthenticationMeansProducerV1")
    public DeutscheBankGroupAuthenticationMeansProducer authenticationMeansProducerV1() {
        return new DeutscheBankGroupAuthenticationMeansProducerV1();
    }

    @Bean("DeutscheBankAuthorizationService")
    public DeutscheBankGroupAuthorizationService authorizationService(Clock clock,
                                                                      @Qualifier("DeutscheBankGroupObjectMapper") ObjectMapper objectMapper) {
        return new DeutscheBankGroupAuthorizationService(
                createDateConverter(clock),
                new DeutscheBankGroupFKDNFormStrategy(clock),
                new DeutscheBankGroupDrivenConsentRequestStrategy(),
                new DeutscheBankGroupProviderStateMapper(objectMapper),
                clock);
    }

    @Bean("DeutscheBankFetchDataService")
    public DeutscheBankGroupFetchDataService fetchDataService(DeutscheBankProperties properties,
                                                              Clock clock,
                                                              @Qualifier("DeutscheBankGroupObjectMapper") ObjectMapper objectMapper) {
        DeutscheBankGroupDateConverter dateConverter = createDateConverter(clock);
        return new DeutscheBankGroupFetchDataService(new DeutscheBankGroupFetchAccountsWithoutBalancesStrategy(),
                new DeutscheBankGroupAccountMapper(dateConverter),
                new DeutscheBankGroupProviderStateMapper(objectMapper),
                new DeutscheBankGroupFetchTransactionsBothStatusStrategy(
                        new DeutscheBankGroupTransactionMapper(dateConverter),
                        properties,
                        dateConverter)
        );
    }

    private DeutscheBankGroupDateConverter createDateConverter(Clock clock) {
        return new DeutscheBankGroupDateConverter(clock, ZoneId.of("Europe/Berlin"));
    }
}
