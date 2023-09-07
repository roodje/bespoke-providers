package com.yolt.providers.monorepogroup.chebancagroup;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.monorepogroup.chebancagroup.common.CheBancaGroupProperties;
import com.yolt.providers.monorepogroup.chebancagroup.common.auth.DefaultCheBancaGroupTypedAuthenticationMeansProducer;
import com.yolt.providers.monorepogroup.chebancagroup.common.dto.internal.ProviderIdentification;
import com.yolt.providers.monorepogroup.chebancagroup.common.http.DefaultCheBancaGroupHttpClientProducer;
import com.yolt.providers.monorepogroup.chebancagroup.common.mapper.CheBancaGroupTokenMapper;
import com.yolt.providers.monorepogroup.chebancagroup.common.mapper.DefaultChaBancaGroupAccountMapper;
import com.yolt.providers.monorepogroup.chebancagroup.common.mapper.DefaultCheBancaGroupDateMapper;
import com.yolt.providers.monorepogroup.chebancagroup.common.mapper.DefaultCheBancaGroupTransactionMapper;
import com.yolt.providers.monorepogroup.chebancagroup.common.mapper.authmeans.DefaultCheBancaGroupAuthMeansMapper;
import com.yolt.providers.monorepogroup.chebancagroup.common.service.DefaultCheBancaGroupAuthorizationService;
import com.yolt.providers.monorepogroup.chebancagroup.common.service.DefaultCheBancaGroupDataMappingService;
import com.yolt.providers.monorepogroup.chebancagroup.common.service.DefaultCheBancaGroupTokenService;
import com.yolt.providers.monorepogroup.chebancagroup.common.service.DefaultChebancaGroupFetchDataService;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

import static com.yolt.providers.monorepogroup.chebancagroup.CheBancaDetailsProvider.CHEBANCA_PROVIDER_KEY;
import static com.yolt.providers.monorepogroup.chebancagroup.CheBancaDetailsProvider.CHEBANCA_PROVIDER_NAME;

@Configuration
public class CheBancaBeanConfig {

    @Bean("CheBancaDataProviderV1")
    public CheBancaGroupDataProvider getCheBancaDataProviderV1(final CheBancaGroupProperties properties,
                                                               final MeterRegistry registry,
                                                               final Clock clock,
                                                               final @Qualifier("CheBancaGroupObjectMapper") ObjectMapper objectMapper) {
        ZoneId zoneId = ZoneId.of("Europe/Rome");
        CheBancaGroupTokenMapper cheBancaGroupTokenMapper = new CheBancaGroupTokenMapper(objectMapper, clock, zoneId);
        DefaultCheBancaGroupDateMapper cheBancaGroupDateMapper = new DefaultCheBancaGroupDateMapper(zoneId, clock);

        return new CheBancaGroupDataProvider(
                new ProviderIdentification(CHEBANCA_PROVIDER_KEY, CHEBANCA_PROVIDER_NAME, ProviderVersion.VERSION_1),
                new DefaultCheBancaGroupTypedAuthenticationMeansProducer(),
                new DefaultCheBancaGroupAuthMeansMapper(),
                new DefaultCheBancaGroupHttpClientProducer(properties, registry, CHEBANCA_PROVIDER_KEY, objectMapper, clock),
                new DefaultCheBancaGroupAuthorizationService(
                        new DefaultCheBancaGroupTokenService(),
                        properties,
                        cheBancaGroupTokenMapper),
                new DefaultChebancaGroupFetchDataService(cheBancaGroupTokenMapper, 100),
                new DefaultCheBancaGroupDataMappingService(
                        new DefaultChaBancaGroupAccountMapper(CHEBANCA_PROVIDER_KEY, cheBancaGroupDateMapper),
                        new DefaultCheBancaGroupTransactionMapper(cheBancaGroupDateMapper)
                ));
    }
}

