package com.yolt.providers.monorepogroup.olbgroup.olb;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.rest.http.DefaultHttpErrorHandlerV2;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.monorepogroup.olbgroup.common.OlbGroupDataProvider;
import com.yolt.providers.monorepogroup.olbgroup.common.auth.OlbGroupAuthenticationMeansProducerV1;
import com.yolt.providers.monorepogroup.olbgroup.common.domain.ProviderIdentification;
import com.yolt.providers.monorepogroup.olbgroup.common.http.OlbGroupHttpClientFactoryV1;
import com.yolt.providers.monorepogroup.olbgroup.common.http.OlbGroupHttpHeadersProducer;
import com.yolt.providers.monorepogroup.olbgroup.common.mapper.OlbGroupAccountMapper;
import com.yolt.providers.monorepogroup.olbgroup.common.mapper.OlbGroupDateConverter;
import com.yolt.providers.monorepogroup.olbgroup.common.mapper.OlbGroupProviderStateMapper;
import com.yolt.providers.monorepogroup.olbgroup.common.mapper.OlbGroupTransactionMapper;
import com.yolt.providers.monorepogroup.olbgroup.common.service.authorization.OlbGroupAuthorizationServiceV1;
import com.yolt.providers.monorepogroup.olbgroup.common.service.fetchdata.OlbGroupFetchDataServiceV1;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

import static com.yolt.providers.monorepogroup.olbgroup.olb.OlbDetailsProvider.OLB_PROVIDER_KEY;
import static com.yolt.providers.monorepogroup.olbgroup.olb.OlbDetailsProvider.OLB_PROVIDER_NAME;

@Configuration
public class OlbBeanConfig {

    @Bean("OlbDataProviderV1")
    public OlbGroupDataProvider getOlbDataProviderV1(MeterRegistry meterRegistry,
                                                     OlbProperties properties,
                                                     @Qualifier("OlbGroupObjectMapper") ObjectMapper objectMapper,
                                                     Clock clock) {
        var httpClientFactory = new OlbGroupHttpClientFactoryV1(objectMapper, meterRegistry, properties, new OlbGroupHttpHeadersProducer(), new DefaultHttpErrorHandlerV2());
        var dateConverter = new OlbGroupDateConverter(clock, ZoneId.of("Europe/Berlin"));
        var stateMapper = new OlbGroupProviderStateMapper(objectMapper);
        return new OlbGroupDataProvider(
                new ProviderIdentification(OLB_PROVIDER_KEY, OLB_PROVIDER_NAME, ProviderVersion.VERSION_1),
                new OlbGroupAuthenticationMeansProducerV1(),
                new OlbGroupAuthorizationServiceV1(
                        stateMapper,
                        clock,
                        dateConverter,
                        httpClientFactory,
                        OLB_PROVIDER_NAME),
                new OlbGroupFetchDataServiceV1(
                        httpClientFactory,
                        new OlbGroupAccountMapper(dateConverter),
                        new OlbGroupTransactionMapper(dateConverter),
                        stateMapper,
                        properties,
                        dateConverter,
                        OLB_PROVIDER_NAME
                ));
    }
}
