package com.yolt.providers.bancacomercialaromana.bcr.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.bancacomercialaromana.bcr.BcrDataProviderV1;
import com.yolt.providers.bancacomercialaromana.common.BcrGroupDataProvider;
import com.yolt.providers.bancacomercialaromana.common.http.BcrGroupHeadersFactory;
import com.yolt.providers.bancacomercialaromana.common.http.BcrGroupHttpClientFactory;
import com.yolt.providers.bancacomercialaromana.common.mapper.BcrGroupAccountMapper;
import com.yolt.providers.bancacomercialaromana.common.mapper.BcrGroupBalanceMapper;
import com.yolt.providers.bancacomercialaromana.common.mapper.BcrGroupTransactionMapper;
import com.yolt.providers.bancacomercialaromana.common.service.BcrGroupAuthorizationServiceV1;
import com.yolt.providers.bancacomercialaromana.common.service.BcrGroupFetchDataServiceV1;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class BcrBeanConfig {

    @Bean(name = "BcrDataProviderV1")
    public BcrGroupDataProvider getDataProvider(BcrProperties properties,
                                                MeterRegistry registry,
                                                @Value("${yolt.qseal-certificate-exposure.base-url}") String s3BaseUrl,
                                                @Qualifier("BcrGroupObjectMapper") ObjectMapper objectMapper,
                                                Clock clock) {
        BcrGroupHeadersFactory bcrGroupHeadersFactory = new BcrGroupHeadersFactory();
        BcrGroupHttpClientFactory bcrGroupHttpClientFactory = new BcrGroupHttpClientFactory(properties, registry, objectMapper, bcrGroupHeadersFactory);
        BcrGroupAuthorizationServiceV1 authorizationService = new BcrGroupAuthorizationServiceV1(properties);
        BcrGroupFetchDataServiceV1 fetchDataService = new BcrGroupFetchDataServiceV1(
                new BcrGroupAccountMapper(new BcrGroupBalanceMapper(), clock),
                new BcrGroupTransactionMapper(),
                properties,
                objectMapper,
                clock);

        return new BcrDataProviderV1(objectMapper,
                authorizationService,
                fetchDataService,
                bcrGroupHttpClientFactory,
                s3BaseUrl,
                clock);
    }
}