package com.yolt.providers.dkbgroup.dkb;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.dkbgroup.common.DKBGroupProvider;
import com.yolt.providers.dkbgroup.common.auth.DKBGroupAuthenticationService;
import com.yolt.providers.dkbgroup.common.auth.DKBGroupTypedAuthenticationMeansProducer;
import com.yolt.providers.dkbgroup.common.dynamicflow.DKBGroupDynamicFlowHandler;
import com.yolt.providers.dkbgroup.common.http.DKBGroupHttpClientFactory;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.text.SimpleDateFormat;
import java.time.Clock;

import static com.yolt.providers.dkbgroup.dkb.DKBDetailsProvider.DKB_DISPLAY_NAME;
import static com.yolt.providers.dkbgroup.dkb.DKBDetailsProvider.DKB_PROVIDER_KEY;

@Configuration
public class DKBBeanConfig {

    @Bean("DKBDataProviderV1")
    public UrlDataProvider getDKBDataProvider(MeterRegistry registry,
                                              Clock clock,
                                              DKBProperties properties) {
        var objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        var dateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm");
        objectMapper.setDateFormat(dateFormat);
        DKBGroupAuthenticationService authenticationService = new DKBGroupAuthenticationService();
        return new DKBGroupProvider(DKB_PROVIDER_KEY,
                DKB_DISPLAY_NAME,
                ProviderVersion.VERSION_1,
                new DKBGroupTypedAuthenticationMeansProducer(),
                new DKBGroupHttpClientFactory(properties, objectMapper, registry),
                authenticationService,
                new DKBGroupDynamicFlowHandler(authenticationService, objectMapper, clock),
                clock);

    }
}
