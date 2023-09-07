package com.yolt.providers.monorepogroup.cecgroup.cec;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.rest.http.DefaultHttpErrorHandlerV3;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.monorepogroup.cecgroup.common.CecGroupDataProvider;
import com.yolt.providers.monorepogroup.cecgroup.common.auth.CecGroupAuthenticationMeansProducerV1;
import com.yolt.providers.monorepogroup.cecgroup.common.domain.ProviderIdentification;
import com.yolt.providers.monorepogroup.cecgroup.common.http.CecGroupHttpBodyProducerV1;
import com.yolt.providers.monorepogroup.cecgroup.common.http.CecGroupHttpClientFactoryV1;
import com.yolt.providers.monorepogroup.cecgroup.common.http.CecGroupHttpHeadersProducerV1;
import com.yolt.providers.monorepogroup.cecgroup.common.mapper.CecGroupAccountMapperV1;
import com.yolt.providers.monorepogroup.cecgroup.common.mapper.CecGroupDateConverter;
import com.yolt.providers.monorepogroup.cecgroup.common.mapper.CecGroupTransactionMapperV1;
import com.yolt.providers.monorepogroup.cecgroup.common.service.CecGroupAuthorizationServiceV1;
import com.yolt.providers.monorepogroup.cecgroup.common.service.CecGroupFetchDataServiceV1;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;
import java.util.Set;

import static com.yolt.providers.monorepogroup.cecgroup.cec.CecDetailsProvider.CEC_PROVIDER_KEY;
import static com.yolt.providers.monorepogroup.cecgroup.cec.CecDetailsProvider.CEC_PROVIDER_NAME;

@Configuration
public class CecBeanConfig {

    @Bean("CecDataProviderV1")
    public CecGroupDataProvider cecDataProviderV1(MeterRegistry meterRegistry,
                                                  CecProperties properties,
                                                  @Qualifier("CecGroupObjectMapper") ObjectMapper objectMapper,
                                                  Clock clock) {
        CecGroupDateConverter dateConverter = new CecGroupDateConverter(ZoneId.of("Europe/Bucharest"));
        return new CecGroupDataProvider(
                new ProviderIdentification(CEC_PROVIDER_KEY, CEC_PROVIDER_NAME, ProviderVersion.VERSION_1),
                new CecGroupAuthenticationMeansProducerV1(),
                new CecGroupHttpClientFactoryV1(objectMapper,
                        meterRegistry,
                        properties,
                        new CecGroupHttpHeadersProducerV1(),
                        new CecGroupHttpBodyProducerV1(),
                        new DefaultHttpErrorHandlerV3()),
                new CecGroupAuthorizationServiceV1(clock, dateConverter),
                new CecGroupFetchDataServiceV1(
                        new CecGroupAccountMapperV1(clock),
                        new CecGroupTransactionMapperV1(),
                        dateConverter,
                        properties
                ),
                dateConverter,
                clock,
                objectMapper,
                properties,
                new ConsentValidityRules(Set.of("User CEConline", "Cod acces"))
        );
    }
}
