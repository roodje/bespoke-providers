package com.yolt.providers.bancatransilvania.bancatransilvania;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.bancatransilvania.common.auth.BancaTransilvaniaGroupAuthenticationMeansProducer;
import com.yolt.providers.bancatransilvania.common.auth.BancaTransilvaniaGroupAuthenticationMeansProducerV1;
import com.yolt.providers.bancatransilvania.common.auth.BancaTransilvaniaGroupPKCE;
import com.yolt.providers.bancatransilvania.common.http.BancaTransilvaniaGroupHttpClientFactory;
import com.yolt.providers.bancatransilvania.common.http.BancaTransilvaniaGroupHttpHeadersProducer;
import com.yolt.providers.bancatransilvania.common.mapper.BancaTransilvaniaGroupAccountMapper;
import com.yolt.providers.bancatransilvania.common.mapper.BancaTransilvaniaGroupProviderStateMapper;
import com.yolt.providers.bancatransilvania.common.mapper.BancaTransilvaniaGroupTransactionMapper;
import com.yolt.providers.bancatransilvania.common.service.BancaTransilvaniaGroupAuthorizationService;
import com.yolt.providers.bancatransilvania.common.service.BancaTransilvaniaGroupFetchDataService;
import com.yolt.providers.bancatransilvania.common.service.BancaTransilvaniaGroupRegistrationService;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class BancaTransilvaniaDataProviderBeanConfig {

    @Bean("BancaTransilvaniaHttpClientFactory")
    public BancaTransilvaniaGroupHttpClientFactory httpClientFactory(MeterRegistry meterRegistry,
                                                                     BancaTransilvaniaProperties properties,
                                                                     @Qualifier("BancaTransilvaniaGroupObjectMapper") ObjectMapper objectMapper) {
        return new BancaTransilvaniaGroupHttpClientFactory(objectMapper, meterRegistry, properties, new BancaTransilvaniaGroupHttpHeadersProducer());
    }

    @Bean("BancaTransilvaniaAuthenticationMeansProducerV1")
    public BancaTransilvaniaGroupAuthenticationMeansProducer authenticationMeansProducerV1() {
        return new BancaTransilvaniaGroupAuthenticationMeansProducerV1();
    }

    @Bean("BancaTransilvaniaAuthorizationService")
    public BancaTransilvaniaGroupAuthorizationService authorizationService(BancaTransilvaniaProperties properties,
                                                                           Clock clock,
                                                                           @Qualifier("BancaTransilvaniaGroupObjectMapper") ObjectMapper objectMapper) {
        return new BancaTransilvaniaGroupAuthorizationService(
                new BancaTransilvaniaGroupPKCE(),
                new BancaTransilvaniaGroupProviderStateMapper(objectMapper, clock),
                properties,
                clock);
    }

    @Bean("BancaTransilvaniaRegistrationService")
    public BancaTransilvaniaGroupRegistrationService registrationService() {
        return new BancaTransilvaniaGroupRegistrationService();
    }

    @Bean("BancaTransilvaniaFetchDataService")
    public BancaTransilvaniaGroupFetchDataService fetchDataService(BancaTransilvaniaProperties properties,
                                                                   Clock clock,
                                                                   @Qualifier("BancaTransilvaniaGroupObjectMapper") ObjectMapper objectMapper) {
        return new BancaTransilvaniaGroupFetchDataService(
                new BancaTransilvaniaGroupAccountMapper(clock),
                new BancaTransilvaniaGroupTransactionMapper(),
                new BancaTransilvaniaGroupProviderStateMapper(objectMapper, clock),
                properties,
                clock);
    }
}
