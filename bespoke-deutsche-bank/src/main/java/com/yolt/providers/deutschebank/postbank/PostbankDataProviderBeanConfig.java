package com.yolt.providers.deutschebank.postbank;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.deutschebank.common.auth.DeutscheBankGroupAuthenticationMeansProducer;
import com.yolt.providers.deutschebank.common.auth.DeutscheBankGroupAuthenticationMeansProducerV1;
import com.yolt.providers.deutschebank.common.config.DeutscheBankGroupDateConverter;
import com.yolt.providers.deutschebank.common.http.DeutscheBankGroupHttpClientFactory;
import com.yolt.providers.deutschebank.common.mapper.DeutscheBankGroupAccountMapper;
import com.yolt.providers.deutschebank.common.mapper.DeutscheBankGroupProviderStateMapper;
import com.yolt.providers.deutschebank.common.mapper.DeutscheBankGroupTransactionMapper;
import com.yolt.providers.deutschebank.common.service.authorization.DeutscheBankGroupAuthorizationService;
import com.yolt.providers.deutschebank.common.service.authorization.consent.DeutscheBankGroupGlobalConsentRequestStrategy;
import com.yolt.providers.deutschebank.common.service.authorization.form.DeutscheBankGroupAlphanumericFormStrategy;
import com.yolt.providers.deutschebank.common.service.fetchdata.DeutscheBankGroupFetchDataService;
import com.yolt.providers.deutschebank.common.service.fetchdata.accounts.DeutscheBankGroupFetchAccountsWithBalancesStrategy;
import com.yolt.providers.deutschebank.common.service.fetchdata.transactions.DeutscheBankGroupFetchTransactionsBothStatusStrategy;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class PostbankDataProviderBeanConfig {

    @Bean("PostbankHttpClientFactory")
    public DeutscheBankGroupHttpClientFactory httpClientFactory(MeterRegistry meterRegistry,
                                                                PostbankProperties properties,
                                                                @Qualifier("DeutscheBankGroupObjectMapper") ObjectMapper objectMapper) {
        return new DeutscheBankGroupHttpClientFactory(objectMapper, meterRegistry, properties);
    }

    @Bean("PostbankAuthenticationMeansProducerV1")
    public DeutscheBankGroupAuthenticationMeansProducer authenticationMeansProducerV1() {
        return new DeutscheBankGroupAuthenticationMeansProducerV1();
    }

    @Bean("PostbankAuthorizationService")
    public DeutscheBankGroupAuthorizationService authorizationService(Clock clock,
                                                                      @Qualifier("DeutscheBankGroupObjectMapper") ObjectMapper objectMapper) {
        return new DeutscheBankGroupAuthorizationService(
                createDateConverter(clock),
                new DeutscheBankGroupAlphanumericFormStrategy(clock),
                new DeutscheBankGroupGlobalConsentRequestStrategy(),
                new DeutscheBankGroupProviderStateMapper(objectMapper),
                clock);
    }

    @Bean("PostbankFetchDataService")
    public DeutscheBankGroupFetchDataService fetchDataService(PostbankProperties properties,
                                                              Clock clock,
                                                              @Qualifier("DeutscheBankGroupObjectMapper") ObjectMapper objectMapper) {
        DeutscheBankGroupDateConverter dateConverter = createDateConverter(clock);
        return new DeutscheBankGroupFetchDataService(new DeutscheBankGroupFetchAccountsWithBalancesStrategy(),
                new DeutscheBankGroupAccountMapper(dateConverter),
                new DeutscheBankGroupProviderStateMapper(objectMapper),
                new DeutscheBankGroupFetchTransactionsBothStatusStrategy(
                        new DeutscheBankGroupTransactionMapper(dateConverter),
                        properties,
                        dateConverter));
    }

    private DeutscheBankGroupDateConverter createDateConverter(Clock clock) {
        return new DeutscheBankGroupDateConverter(clock, ZoneId.of("Europe/Berlin"));
    }
}
