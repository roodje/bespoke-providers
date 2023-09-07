package com.yolt.providers.brdgroup.brd;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.brdgroup.common.BrdGroupDataProvider;
import com.yolt.providers.brdgroup.common.authorization.AuthorizationService;
import com.yolt.providers.brdgroup.common.authorization.AuthorizationServiceV1;
import com.yolt.providers.brdgroup.common.authorization.BrdGroupConsentStatusValidator;
import com.yolt.providers.brdgroup.common.authorization.BrdGroupConsentStatusValidatorV1;
import com.yolt.providers.brdgroup.common.config.ProviderIdentification;
import com.yolt.providers.brdgroup.common.fetchdata.FetchDataService;
import com.yolt.providers.brdgroup.common.fetchdata.FetchDataServiceV1;
import com.yolt.providers.brdgroup.common.http.BrdGroupHttpClientFactory;
import com.yolt.providers.brdgroup.common.http.BrdGroupHttpClientFactoryV1;
import com.yolt.providers.brdgroup.common.mapper.AccountMapper;
import com.yolt.providers.brdgroup.common.mapper.AccountMapperV1;
import com.yolt.providers.brdgroup.common.mapper.TransactionMapper;
import com.yolt.providers.brdgroup.common.mapper.TransactionMapperV1;
import com.yolt.providers.brdgroup.common.util.BrdGroupDateConverter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;
import java.util.concurrent.Executors;

import static com.yolt.providers.brdgroup.brd.BrdDetailsProvider.BRD_PROVIDER_KEY;
import static com.yolt.providers.brdgroup.brd.BrdDetailsProvider.BRD_PROVIDER_NAME;
import static com.yolt.providers.common.versioning.ProviderVersion.VERSION_1;

@Configuration
public class BrdBeanConfig {

    @Bean("BrdDataProviderV1")
    public BrdGroupDataProvider brdDataProviderV1(Clock clock,
                                                  MeterRegistry meterRegistry,
                                                  @Qualifier("BrdGroupObjectMapper") ObjectMapper objectMapper,
                                                  BrdProperties properties) {
        ProviderIdentification providerIdentification = new ProviderIdentification(BRD_PROVIDER_KEY,
                BRD_PROVIDER_NAME,
                VERSION_1);
        BrdGroupConsentStatusValidator consentStatusValidator = new BrdGroupConsentStatusValidatorV1(
                Executors.newScheduledThreadPool(properties.getCorePoolSize()),
                properties.getConsentStatusPollingTotalDelayLimitInSeconds(),
                properties.getConsentStatusPollingInitialDelayInSeconds()
        );
        BrdGroupDateConverter dateConverter = new BrdGroupDateConverter(clock, ZoneId.of("Europe/Bucharest"));
        AuthorizationService authorizationService = new AuthorizationServiceV1(consentStatusValidator, clock,
                dateConverter, objectMapper);

        TransactionMapper transactionMapper = new TransactionMapperV1(dateConverter);
        AccountMapper accountMapper = new AccountMapperV1(dateConverter);
        FetchDataService fetchDataService = new FetchDataServiceV1(accountMapper, transactionMapper, dateConverter, properties);
        BrdGroupHttpClientFactory httpClientFactory = new BrdGroupHttpClientFactoryV1(objectMapper,
                meterRegistry, properties);

        return new BrdGroupDataProvider(httpClientFactory,
                authorizationService,
                fetchDataService,
                providerIdentification,
                objectMapper);
    }
}
