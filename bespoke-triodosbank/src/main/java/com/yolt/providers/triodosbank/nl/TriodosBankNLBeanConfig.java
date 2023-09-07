package com.yolt.providers.triodosbank.nl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.triodosbank.common.mapper.TriodosBankAccountMapper;
import com.yolt.providers.triodosbank.common.mapper.TriodosBankBalanceMapper;
import com.yolt.providers.triodosbank.common.mapper.TriodosBankTransactionMapper;
import com.yolt.providers.triodosbank.common.rest.TriodosBankHttpClientFactory;
import com.yolt.providers.triodosbank.common.service.TriodosBankAuthorizationService;
import com.yolt.providers.triodosbank.common.service.TriodosBankFetchDataService;
import com.yolt.providers.triodosbank.common.service.TriodosBankRegistrationService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class TriodosBankNLBeanConfig {

    private static final ZoneId ZONE_ID = ZoneId.of("Europe/Amsterdam");

    @Bean("TriodosBankNLRegistrationService")
    public TriodosBankRegistrationService getRegistrationService() {
        return new TriodosBankRegistrationService();
    }

    @Bean("TriodosBankNLAuthorizationService")
    public TriodosBankAuthorizationService getAuthorizationService(Clock clock) {
        return new TriodosBankAuthorizationService(getClock(clock));
    }

    @Bean("TriodosBankNLFetchDataService")
    public TriodosBankFetchDataService getFetchDataService(TriodosBankNLProperties properties, Clock clock){
        TriodosBankAccountMapper accountMapper = new TriodosBankAccountMapper(getClock(clock), new TriodosBankBalanceMapper(getClock(clock)));
        TriodosBankTransactionMapper transactionMapper = new TriodosBankTransactionMapper(getClock(clock));
        return new TriodosBankFetchDataService(accountMapper, transactionMapper, properties);
    }

    @Bean("TriodosBankNLHttpClientFactory")
    public TriodosBankHttpClientFactory getHttpClientFactory(TriodosBankNLProperties properties,
                                                             @Qualifier("TriodosBankObjectMapper") ObjectMapper objectMapper) {
        return new TriodosBankHttpClientFactory(properties, objectMapper);
    }

    private Clock getClock(Clock clock) {
        return clock.withZone(ZONE_ID);
    }
}
