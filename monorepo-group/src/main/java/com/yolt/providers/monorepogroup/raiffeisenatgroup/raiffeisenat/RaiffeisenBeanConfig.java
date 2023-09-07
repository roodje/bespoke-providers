package com.yolt.providers.monorepogroup.raiffeisenatgroup.raiffeisenat;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.RaiffeisenAtGroupDataProvider;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.auth.typedauthmeans.DefaultRaiffeisenAtGroupTypedAuthenticationMeansProducer;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.dto.internal.ProviderIdentification;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.http.DefaultRaiffeisenAtGroupHttpClientProducer;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.mapper.DefaultRaiffeisenAtGroupAccountMapper;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.mapper.DefaultRaiffeisenAtGroupDateMapper;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.mapper.DefaultRaiffeisenAtGroupTransactionMapper;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.mapper.RaiffeisenAtGroupDateMapper;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.mapper.authmeans.DefaultRaiffeisenAtGroupAuthMeansMapper;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.mapper.authmeans.DefaultRaiffeisenAtGroupProviderStateMapper;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.mapper.authmeans.RaiffeisenAtGroupProviderStateMapper;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.service.*;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;
import java.util.Collections;

@Configuration
public class RaiffeisenBeanConfig {

    public static final String PROVIDER_IDENTIFIER = "RAIFFEISEN_AT";
    public static final String PROVIDER_DISPLAY_NAME = "Raiffeisen Meine Bank";

    @Bean("RaiffeisenAtDataProviderV1")
    UrlDataProvider getRaiffeisenAtDataProviderV1(RaiffeisenAtProperties properties,
                                                  MeterRegistry registry,
                                                  Clock clock,
                                                  @Qualifier("RaiffeisenAtGroupObjectMapper") ObjectMapper objectMapper) {
        RaiffeisenAtGroupTokenService tokenService = new DefaultRaiffeisenAtGroupTokenService();
        RaiffeisenAtGroupProviderStateMapper providerStateMapper = new DefaultRaiffeisenAtGroupProviderStateMapper(objectMapper);
        ZoneId zoneId = ZoneId.of("Europe/Vienna");
        RaiffeisenAtGroupDateMapper dateMapper = new DefaultRaiffeisenAtGroupDateMapper(zoneId, clock);
        return new RaiffeisenAtGroupDataProvider(
                new ProviderIdentification(PROVIDER_IDENTIFIER, PROVIDER_DISPLAY_NAME, ProviderVersion.VERSION_1),
                new DefaultRaiffeisenAtGroupTypedAuthenticationMeansProducer(),
                new DefaultRaiffeisenAtGroupAuthMeansMapper(),
                new DefaultRaiffeisenAtGroupHttpClientProducer(properties, registry, PROVIDER_IDENTIFIER, objectMapper, zoneId, clock),
                new DefaultRaiffeisenAtGroupAuthorizationService(
                        tokenService,
                        providerStateMapper,
                        dateMapper,
                        clock
                ),
                new DefaultRaiffeisenAtGroupFetchDataService(providerStateMapper, tokenService, properties.getPaginationLimit()),
                new DefaultRaiffeisenAtGroupDataMappingService(
                        new DefaultRaiffeisenAtGroupAccountMapper(PROVIDER_DISPLAY_NAME, dateMapper),
                        new DefaultRaiffeisenAtGroupTransactionMapper(dateMapper)),
                new DefaultRaiffeisenAtGroupRegistrationService(PROVIDER_IDENTIFIER),
                new ConsentValidityRules(Collections.singleton("'/mein-login'")));
    }
}
