package com.yolt.providers.axabanque.crelan.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.axabanque.common.GroupDataProvider;
import com.yolt.providers.axabanque.common.auth.http.clientproducer.DefaultHttpClientProducer;
import com.yolt.providers.axabanque.common.auth.http.clientproducer.HttpClientProducer;
import com.yolt.providers.axabanque.common.auth.http.headerproducer.DefaultAuthorizationRequestHeadersProducer;
import com.yolt.providers.axabanque.common.auth.mapper.access.DefaultAccessMeansMapper;
import com.yolt.providers.axabanque.common.auth.mapper.access.DefaultAccessTokenMapper;
import com.yolt.providers.axabanque.common.auth.mapper.authentication.DefaultAuthenticationMeansMapper;
import com.yolt.providers.axabanque.common.auth.service.AuthenticationService;
import com.yolt.providers.axabanque.common.auth.service.DefaultAuthenticationService;
import com.yolt.providers.axabanque.common.auth.typedauthmeans.OnlyTransportTypedAuthenticationMeansProducer;
import com.yolt.providers.axabanque.common.consentwindow.ThreeMonthsAfterFifteenMinutesConsentWindow;
import com.yolt.providers.axabanque.common.fetchdata.errorhandler.DefaultFetchDataHttpErrorHandlerV2;
import com.yolt.providers.axabanque.common.fetchdata.http.headerproducer.DefaultFetchDataHeadersProducer;
import com.yolt.providers.axabanque.common.fetchdata.mapper.DefaultAccountMapper;
import com.yolt.providers.axabanque.common.fetchdata.mapper.DefaultAccountTypeMapper;
import com.yolt.providers.axabanque.common.fetchdata.mapper.DefaultTransactionMapper;
import com.yolt.providers.axabanque.common.fetchdata.service.DefaultFetchDataService;
import com.yolt.providers.axabanque.common.fetchdata.service.FetchDataService;
import com.yolt.providers.axabanque.common.pkce.PKCE;
import com.yolt.providers.axabanque.common.requirements.TransportKeyRequirementsProducer;
import com.yolt.providers.axabanque.common.traceid.TraceIdProducer;
import com.yolt.providers.common.mapper.currency.NumericCodeCurrencyCodeMapper;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.common.rest.http.DefaultHttpErrorHandlerV2;
import com.yolt.providers.common.rest.http.HttpErrorHandlerV2;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

import static com.yolt.providers.common.versioning.ProviderVersion.VERSION_1;

@Configuration
public class CrelanBeanConfig {

    private DefaultHttpClientProducer getCrelanHttpClientProducer(CrelanProperties properties, MeterRegistry registry) {
        HttpErrorHandlerV2 errorHandlerV2 = new DefaultHttpErrorHandlerV2();
        return new DefaultHttpClientProducer(
                registry,
                new DefaultAuthorizationRequestHeadersProducer(),
                new DefaultFetchDataHeadersProducer(),
                new DefaultHttpErrorHandlerV2(),
                new DefaultFetchDataHttpErrorHandlerV2(errorHandlerV2),
                properties,
                "v1",
                CrelanDetailsProvider.PROVIDER_DISPLAY_NAME);
    }

    private AuthenticationService getCrelanAuthenticationService(final Clock clock, CrelanProperties properties, HttpClientProducer restTemplateProducer, ObjectMapper objectMapper) {
        return new DefaultAuthenticationService(
                clock,
                new PKCE(),
                restTemplateProducer,
                properties,
                objectMapper,
                new DefaultAccessMeansMapper(),
                new DefaultAccessTokenMapper(),
                new TraceIdProducer());
    }

    private FetchDataService getCrelanFetchDataService(CrelanProperties properties, HttpClientProducer restTemplateProducer, ZoneId zoneId, Clock clock) {
        return new DefaultFetchDataService(restTemplateProducer,
                new DefaultAccountMapper(zoneId, new DefaultTransactionMapper(zoneId), new DefaultAccountTypeMapper(), new NumericCodeCurrencyCodeMapper(), clock),
                properties.getTransactionPaginationLimit(),
                new ThreeMonthsAfterFifteenMinutesConsentWindow(clock));
    }

    @Bean("CrelanDataProviderV1")
    public UrlDataProvider getCrelanDataProvider(@Qualifier("AxaGroupObjectMapper") ObjectMapper objectMapper,
                                                 CrelanProperties properties,
                                                 Clock clock,
                                                 MeterRegistry registry) {
        HttpClientProducer restTemplateProducer = getCrelanHttpClientProducer(properties, registry);
        AuthenticationService authenticationService = getCrelanAuthenticationService(clock, properties, restTemplateProducer, objectMapper);
        ZoneId zoneId = ZoneId.of("Europe/Brussels");
        FetchDataService fetchDataService = getCrelanFetchDataService(properties, restTemplateProducer, zoneId, clock);
        return new GroupDataProvider(
                authenticationService,
                fetchDataService,
                new TransportKeyRequirementsProducer(),
                new OnlyTransportTypedAuthenticationMeansProducer(),
                new DefaultAuthenticationMeansMapper(),
                objectMapper,
                CrelanDetailsProvider.PROVIDER_KEY,
                CrelanDetailsProvider.PROVIDER_DISPLAY_NAME,
                VERSION_1);
    }
}
