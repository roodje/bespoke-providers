package com.yolt.providers.monorepogroup.qontogroup.qonto;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.common.rest.http.DefaultHttpErrorHandlerV3;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.monorepogroup.qontogroup.common.QontoGroupDataProvider;
import com.yolt.providers.monorepogroup.qontogroup.common.auth.typedmeans.DefaultQontoGroupTypedAuthMeansProducer;
import com.yolt.providers.monorepogroup.qontogroup.common.dto.internal.ProviderIdentification;
import com.yolt.providers.monorepogroup.qontogroup.common.filter.DefaultQontoGroupAccountFilter;
import com.yolt.providers.monorepogroup.qontogroup.common.filter.DefaultQontoGroupTransactionFilter;
import com.yolt.providers.monorepogroup.qontogroup.common.http.DefaultQontoGroupHttpClientProducer;
import com.yolt.providers.monorepogroup.qontogroup.common.mapper.DefaultQontoGroupAccountMapper;
import com.yolt.providers.monorepogroup.qontogroup.common.mapper.DefaultQontoGroupTransactionMapper;
import com.yolt.providers.monorepogroup.qontogroup.common.mapper.QontoGroupDateMapper;
import com.yolt.providers.monorepogroup.qontogroup.common.mapper.auth.DefaultQontoGroupAuthenticationMeansMapper;
import com.yolt.providers.monorepogroup.qontogroup.common.mapper.auth.DefaultQontoGroupProviderStateMapper;
import com.yolt.providers.monorepogroup.qontogroup.common.service.DefaultQontoFetchDataService;
import com.yolt.providers.monorepogroup.qontogroup.common.service.DefaultQontoGroupAuthenticationService;
import com.yolt.providers.monorepogroup.qontogroup.common.service.DefaultQontoGroupMappingService;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class QontoBeanConfig {

    public static final String PROVIDER_IDENTIFIER = "QONTO";
    public static final String PROVIDER_DISPLAY_NAME = "Qonto";

    @Bean("QontoDataProviderV1")
    UrlDataProvider getQontoDataProviderV1(QontoProperties properties,
                                           MeterRegistry registry,
                                           Clock clock,
                                           @Qualifier("QontoGroupObjectMapper") ObjectMapper objectMapper) {
        QontoGroupDateMapper dateMapper = new QontoGroupDateMapper(ZoneId.of("Europe/Paris"), clock);
        return new QontoGroupDataProvider(
                clock,
                new ProviderIdentification(PROVIDER_IDENTIFIER, PROVIDER_DISPLAY_NAME, ProviderVersion.VERSION_1),
                new DefaultQontoGroupTypedAuthMeansProducer(),
                new DefaultQontoGroupAuthenticationMeansMapper(properties.getS3baseUrl()),
                new DefaultQontoGroupAuthenticationService(properties.getAuthorizationUrl(), clock),
                new DefaultQontoGroupHttpClientProducer(properties, registry, objectMapper, new DefaultHttpErrorHandlerV3(), dateMapper),
                new DefaultQontoGroupProviderStateMapper(objectMapper),
                new DefaultQontoFetchDataService(properties.getPaginationLimit(), new DefaultQontoGroupAccountFilter(), new DefaultQontoGroupTransactionFilter()),
                new DefaultQontoGroupMappingService(
                        new DefaultQontoGroupAccountMapper(PROVIDER_DISPLAY_NAME, dateMapper),
                        new DefaultQontoGroupTransactionMapper(dateMapper)
                )
        );
    }
}
