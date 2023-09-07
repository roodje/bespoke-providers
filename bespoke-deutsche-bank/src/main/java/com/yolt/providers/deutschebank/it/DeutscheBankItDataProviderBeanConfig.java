package com.yolt.providers.deutschebank.it;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.rest.http.DefaultHttpErrorHandlerV2;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.deutschebank.common.DeutscheBankGroupDataProviderV2;
import com.yolt.providers.deutschebank.common.auth.DeutscheBankGroupAuthenticationMeansProducerV1;
import com.yolt.providers.deutschebank.common.config.DeutscheBankGroupDateConverter;
import com.yolt.providers.deutschebank.common.http.DeutscheBankGroupHttpClientFactoryV2;
import com.yolt.providers.deutschebank.common.mapper.DeutscheBankGroupAccountMapper;
import com.yolt.providers.deutschebank.common.mapper.DeutscheBankGroupProviderStateMapper;
import com.yolt.providers.deutschebank.common.mapper.DeutscheBankGroupTransactionMapper;
import com.yolt.providers.deutschebank.common.service.authorization.DeutscheBankGroupAuthorizationService;
import com.yolt.providers.deutschebank.common.service.authorization.consent.DeutscheBankGroupGlobalConsentRequestStrategy;
import com.yolt.providers.deutschebank.common.service.authorization.form.DeutscheBankGroupEmailFormStrategy;
import com.yolt.providers.deutschebank.common.service.fetchdata.DeutscheBankGroupFetchDataService;
import com.yolt.providers.deutschebank.common.service.fetchdata.accounts.DeutscheBankGroupFetchAccountsWithBalancesStrategy;
import com.yolt.providers.deutschebank.common.service.fetchdata.transactions.DeutscheBankGroupFetchTransactionsIndicatedStatusStrategy;
import com.yolt.providers.deutschebank.it.http.DeutscheBankItHttpHeadersProducer;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;
import java.util.List;

import static com.yolt.providers.deutschebank.it.DeutscheBankItDetailsProvider.DEUTSCHE_BANK_IT_PROVIDER_KEY;
import static com.yolt.providers.deutschebank.it.DeutscheBankItDetailsProvider.DEUTSCHE_BANK_IT_PROVIDER_NAME;
import static nl.ing.lovebird.extendeddata.transaction.TransactionStatus.BOOKED;

@Configuration
public class DeutscheBankItDataProviderBeanConfig {

    @Bean("DeutscheBankItDataProviderV1")
    public DeutscheBankGroupDataProviderV2 createDeutscheBankItDataProviderV1(MeterRegistry meterRegistry,
                                                                              DeutscheBankItProperties properties,
                                                                              @Qualifier("DeutscheBankGroupObjectMapper") ObjectMapper objectMapper,
                                                                              Clock clock) {
        DeutscheBankGroupDateConverter dateConverter = new DeutscheBankGroupDateConverter(clock, ZoneId.of("Europe/Rome"));

        DeutscheBankGroupAuthorizationService authorizationService = new DeutscheBankGroupAuthorizationService(
                dateConverter,
                new DeutscheBankGroupEmailFormStrategy(clock),
                new DeutscheBankGroupGlobalConsentRequestStrategy(),
                new DeutscheBankGroupProviderStateMapper(objectMapper),
                clock);

        DeutscheBankGroupFetchDataService fetchDataService = new DeutscheBankGroupFetchDataService(new DeutscheBankGroupFetchAccountsWithBalancesStrategy(),
                new DeutscheBankGroupAccountMapper(dateConverter),
                new DeutscheBankGroupProviderStateMapper(objectMapper),
                new DeutscheBankGroupFetchTransactionsIndicatedStatusStrategy(
                        new DeutscheBankGroupTransactionMapper(dateConverter),
                        properties,
                        dateConverter,
                        List.of(BOOKED)));

        return new DeutscheBankGroupDataProviderV2(
                new DeutscheBankGroupHttpClientFactoryV2(
                        objectMapper,
                        meterRegistry,
                        properties,
                        new DeutscheBankItHttpHeadersProducer(properties),
                        new DefaultHttpErrorHandlerV2()),
                authorizationService,
                fetchDataService,
                new DeutscheBankGroupAuthenticationMeansProducerV1(),
                ConsentValidityRules.EMPTY_RULES_SET,
                DEUTSCHE_BANK_IT_PROVIDER_KEY,
                DEUTSCHE_BANK_IT_PROVIDER_NAME,
                ProviderVersion.VERSION_1
        );
    }
}
