package com.yolt.providers.axabanque.comdirect.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.axabanque.comdirect.mapper.ComdirectAccountTypeMapper;
import com.yolt.providers.axabanque.common.GroupDataProvider;
import com.yolt.providers.axabanque.common.auth.http.clientproducer.DefaultHttpClientProducer;
import com.yolt.providers.axabanque.common.auth.http.clientproducer.HttpClientProducer;
import com.yolt.providers.axabanque.common.auth.http.headerproducer.DefaultAuthorizationRequestHeadersProducer;
import com.yolt.providers.axabanque.common.auth.mapper.access.DefaultAccessMeansMapper;
import com.yolt.providers.axabanque.common.auth.mapper.access.DefaultAccessTokenMapper;
import com.yolt.providers.axabanque.common.auth.mapper.authentication.DefaultAuthenticationMeansMapper;
import com.yolt.providers.axabanque.common.auth.service.AuthenticationService;
import com.yolt.providers.axabanque.common.auth.service.DefaultAuthenticationService;
import com.yolt.providers.axabanque.common.auth.typedauthmeans.DefaultTypedAuthenticationMeansProducer;
import com.yolt.providers.axabanque.common.consentwindow.ThreeMonthsAfterFifteenMinutesConsentWindow;
import com.yolt.providers.axabanque.common.fetchdata.errorhandler.DefaultFetchDataHttpErrorHandlerV2;
import com.yolt.providers.axabanque.common.fetchdata.http.headerproducer.DefaultFetchDataHeadersProducer;
import com.yolt.providers.axabanque.common.fetchdata.mapper.DefaultAccountMapper;
import com.yolt.providers.axabanque.common.fetchdata.mapper.DefaultTransactionMapper;
import com.yolt.providers.axabanque.common.fetchdata.service.DefaultFetchDataService;
import com.yolt.providers.axabanque.common.fetchdata.service.FetchDataService;
import com.yolt.providers.axabanque.common.pkce.PKCE;
import com.yolt.providers.axabanque.common.requirements.TransportKeyRequirementsProducer;
import com.yolt.providers.axabanque.common.traceid.TraceIdProducer;
import com.yolt.providers.common.mapper.currency.SymbolicCurrencyCodeMapper;
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
public class ComdirectBeanConfig {
    private DefaultHttpClientProducer getComdirectHttpClientProducer(ComdirectProperties properties, MeterRegistry meterRegistry) {
        HttpErrorHandlerV2 errorHandler = new DefaultHttpErrorHandlerV2();
        return new DefaultHttpClientProducer(
                meterRegistry,
                new DefaultAuthorizationRequestHeadersProducer(),
                new DefaultFetchDataHeadersProducer(),
                errorHandler,
                new DefaultFetchDataHttpErrorHandlerV2(errorHandler),
                properties,
                "v1",
                "COMDIRECT");
    }

    private AuthenticationService getComdirectAuthenticationService(final Clock clock, ComdirectProperties properties, HttpClientProducer restTemplateProducer, ObjectMapper objectMapper) {
        return new DefaultAuthenticationService(
                clock,
                new PKCE(), restTemplateProducer, properties,
                objectMapper,
                new DefaultAccessMeansMapper(),
                new DefaultAccessTokenMapper(),
                new TraceIdProducer());
    }

    private FetchDataService getComdirectFetchDataService(ComdirectProperties properties, HttpClientProducer restTemplateProducer, ZoneId zoneId, Clock clock) {
        return new DefaultFetchDataService(restTemplateProducer,
                new DefaultAccountMapper(zoneId, new DefaultTransactionMapper(zoneId), new ComdirectAccountTypeMapper(), new SymbolicCurrencyCodeMapper(), clock),
                properties.getTransactionPaginationLimit(),
                new ThreeMonthsAfterFifteenMinutesConsentWindow(clock));
    }

    @Bean("ComdirectDataProviderV1")
    public UrlDataProvider getComdirectDataProvider(@Qualifier("AxaGroupObjectMapper") ObjectMapper objectMapper,
                                                    ComdirectProperties properties,
                                                    Clock clock,
                                                    MeterRegistry meterRegistry) {
        HttpClientProducer restTemplateProducer = getComdirectHttpClientProducer(properties, meterRegistry);
        AuthenticationService authenticationService = getComdirectAuthenticationService(clock, properties, restTemplateProducer, objectMapper);
        ZoneId zoneId = ZoneId.of("Europe/Berlin");
        FetchDataService fetchDataService = getComdirectFetchDataService(properties, restTemplateProducer, zoneId, clock);
        return new GroupDataProvider(
                authenticationService,
                fetchDataService,
                new TransportKeyRequirementsProducer(),
                new DefaultTypedAuthenticationMeansProducer(),
                new DefaultAuthenticationMeansMapper(),
                objectMapper,
                "COMDIRECT",
                "Comdirect",
                VERSION_1);
    }
}
