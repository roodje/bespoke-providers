package com.yolt.providers.fabric.bancasella.beanconfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.mapper.currency.SymbolicCurrencyCodeMapper;
import com.yolt.providers.common.rest.http.DefaultHttpErrorHandlerV2;
import com.yolt.providers.common.rest.http.HttpErrorHandlerV2;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.fabric.bancasella.BancaSellaDetailsProvider;
import com.yolt.providers.fabric.common.FabricGroupDataProviderV1;
import com.yolt.providers.fabric.common.auth.AuthenticationService;
import com.yolt.providers.fabric.common.beanconfig.FabricGroupProperties;
import com.yolt.providers.fabric.common.fetchdata.DefaultFetchDataService;
import com.yolt.providers.fabric.common.http.DefaultAuthorizationRequestHeadersProducer;
import com.yolt.providers.fabric.common.http.FabricGroupHttpClientFactory;
import com.yolt.providers.fabric.common.mapper.DefaultAccountMapper;
import com.yolt.providers.fabric.common.mapper.DefaultAccountTypeMapper;
import com.yolt.providers.fabric.common.mapper.DefaultTransactionMapper;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class BancaSellaBeanConfig {

    private AuthenticationService getAuthenticationService(final Clock clock,
                                                           final @Qualifier("FabricGroupObjectMapper") ObjectMapper objectMapper) {
        return new AuthenticationService(
                clock,
                objectMapper);
    }

    private DefaultFetchDataService getFetchDataService(final Clock clock,
                                                        final @Qualifier("FabricGroupObjectMapper") ObjectMapper objectMapper,
                                                        final BancaSellaProperties properties) {
        return new DefaultFetchDataService(
                new DefaultAccountMapper(clock.getZone(), new DefaultTransactionMapper(clock.getZone()), new DefaultAccountTypeMapper(), new SymbolicCurrencyCodeMapper(), clock));
    }

    @Bean("BancaSellaDataProviderV1")
    public FabricGroupDataProviderV1 getDataProviderV1(final BancaSellaProperties properties,
                                                       final Clock clock,
                                                       final MeterRegistry registry,
                                                       final @Qualifier("FabricGroupObjectMapper") ObjectMapper objectMapper) {

        FabricGroupHttpClientFactory restTemplateProducer = getBancaSellaHttpClientProducer(registry, properties, objectMapper);
        AuthenticationService authenticationService = getAuthenticationService(clock, objectMapper);
        DefaultFetchDataService fetchDataService = getFetchDataService(clock, objectMapper, properties);
        return new FabricGroupDataProviderV1(
                properties,
                authenticationService,
                fetchDataService,
                objectMapper,
                restTemplateProducer,
                BancaSellaDetailsProvider.PROVIDER_IDENTIFIER,
                BancaSellaDetailsProvider.PROVIDER_DISPLAY_NAME,
                ProviderVersion.VERSION_1);
    }

    @Bean("HttpUtilProducer")
    public FabricGroupHttpClientFactory getBancaSellaHttpClientProducer(final MeterRegistry registry,
                                                                        final FabricGroupProperties properties,
                                                                        final @Qualifier("FabricGroupObjectMapper") ObjectMapper objectMapper) {
        HttpErrorHandlerV2 errorHandler = new DefaultHttpErrorHandlerV2();
        return new FabricGroupHttpClientFactory(
                objectMapper,
                properties,
                registry,
                new DefaultAuthorizationRequestHeadersProducer(),
                "v1",
                BancaSellaDetailsProvider.PROVIDER_DISPLAY_NAME,
                errorHandler);
    }
}
