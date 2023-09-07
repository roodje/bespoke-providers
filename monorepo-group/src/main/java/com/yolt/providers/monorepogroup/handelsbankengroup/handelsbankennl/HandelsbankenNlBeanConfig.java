package com.yolt.providers.monorepogroup.handelsbankengroup.handelsbankennl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.rest.http.DefaultHttpErrorHandlerV3;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.monorepogroup.handelsbankengroup.common.HandelsbankenGroupDataProvider;
import com.yolt.providers.monorepogroup.handelsbankengroup.common.HandelsbankenGroupDateConverter;
import com.yolt.providers.monorepogroup.handelsbankengroup.common.auth.HandelsbankenGroupAuthMeansProducerV1;
import com.yolt.providers.monorepogroup.handelsbankengroup.common.auth.HandelsbankenGroupAuthServiceV1;
import com.yolt.providers.monorepogroup.handelsbankengroup.common.domain.ProviderIdentification;
import com.yolt.providers.monorepogroup.handelsbankengroup.common.http.HandelsbankenGroupHttpBodyProducerV1;
import com.yolt.providers.monorepogroup.handelsbankengroup.common.http.HandelsbankenGroupHttpClientFactoryV1;
import com.yolt.providers.monorepogroup.handelsbankengroup.common.http.HandelsbankenGroupHttpHeadersProducerV1;
import com.yolt.providers.monorepogroup.handelsbankengroup.common.onboarding.HandelsbankenGroupOnboardingServiceV1;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

import static com.yolt.providers.monorepogroup.handelsbankengroup.handelsbankennl.HandelsbankenNlDetailsProvider.HANDELSBANKEN_NL_PROVIDER_KEY;
import static com.yolt.providers.monorepogroup.handelsbankengroup.handelsbankennl.HandelsbankenNlDetailsProvider.HANDELSBANKEN_NL_PROVIDER_NAME;

@Configuration
public class HandelsbankenNlBeanConfig {

    @Bean("HandelsbankenNlDataProviderV1")
    public HandelsbankenGroupDataProvider handelsbankenNlDataProvider(MeterRegistry meterRegistry,
                                                                      HandelsbankenNlProperties properties,
                                                                      @Qualifier("HandelsbankenGroupObjectMapper") ObjectMapper objectMapper,
                                                                      Clock clock) {
        return new HandelsbankenGroupDataProvider(
                new ProviderIdentification(HANDELSBANKEN_NL_PROVIDER_KEY, HANDELSBANKEN_NL_PROVIDER_NAME, ProviderVersion.VERSION_1),
                new HandelsbankenGroupAuthMeansProducerV1(),
                new HandelsbankenGroupOnboardingServiceV1(),
                new HandelsbankenGroupAuthServiceV1(properties, clock),
                new HandelsbankenGroupHttpClientFactoryV1(
                        objectMapper,
                        meterRegistry,
                        properties,
                        new HandelsbankenGroupHttpHeadersProducerV1("NL"),
                        new HandelsbankenGroupHttpBodyProducerV1(),
                        new DefaultHttpErrorHandlerV3()
                ),
                new HandelsbankenGroupDateConverter(ZoneId.of("Europe/Amsterdam")),
                objectMapper,
                clock
        );
    }
}
