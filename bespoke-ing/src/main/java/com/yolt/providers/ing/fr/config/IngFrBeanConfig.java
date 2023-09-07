package com.yolt.providers.ing.fr.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.initiate.SepaInitiatePaymentExecutionContextAdapterBuilder;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.status.SepaStatusPaymentExecutionContextAdapterBuilder;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.submit.SepaSubmitPaymentExecutionContextAdapterBuilder;
import com.yolt.providers.common.rest.tracing.ExternalTracingUtil;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.ing.common.IngDataProviderV9;
import com.yolt.providers.ing.common.IngPaymentProviderV2;
import com.yolt.providers.ing.common.config.IngObjectMapper;
import com.yolt.providers.ing.common.dto.InitiatePaymentResponse;
import com.yolt.providers.ing.common.dto.PaymentStatusResponse;
import com.yolt.providers.ing.common.dto.SepaCreditTransfer;
import com.yolt.providers.ing.common.exception.DefaultPisHttpClientErrorHandler;
import com.yolt.providers.ing.common.http.HttpClientFactory;
import com.yolt.providers.ing.common.pec.*;
import com.yolt.providers.ing.common.pec.initiate.*;
import com.yolt.providers.ing.common.pec.status.DefaultStatusPaymentHttpRequestInvoker;
import com.yolt.providers.ing.common.pec.status.DefaultStatusPaymentPreExecutionResultMapper;
import com.yolt.providers.ing.common.pec.submit.*;
import com.yolt.providers.ing.common.service.*;
import com.yolt.providers.ing.it.config.IngItProperties;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;
import java.util.function.Supplier;

@Configuration
public class IngFrBeanConfig {

    @Bean("IngFrDataProviderV10")
    public IngDataProviderV9 getDataProviderV10(IngFrProperties properties, Clock clock) {
        IngClientAwareRestTemplateService restTemplateService = new IngClientAwareRestTemplateService(properties);
        IngSigningUtil ingSigningUtil = new IngSigningUtil();
        IngFetchDataService fetchDataService = new IngFetchDataServiceV9(new IngDataMapperServiceV6(IngFrProperties.ZONE_ID), restTemplateService, ingSigningUtil, properties, clock);
        IngAuthenticationServiceV3 authenticationService = new IngAuthenticationServiceV3(restTemplateService, ingSigningUtil, properties, IngFrProperties.COUNTRY_CODE, clock);
        return new IngDataProviderV9(
                fetchDataService,
                authenticationService,
                IngObjectMapper.get(),
                IngFrProperties.PROVIDER_IDENTIFIER,
                IngFrProperties.PROVIDER_IDENTIFIER_DISPLAY_NAME,
                ProviderVersion.VERSION_10,
                clock);
    }

    @Bean("IngFrPaymentProviderV2")
    public IngPaymentProviderV2 getPaymentProviderV2(final IngFrProperties properties,
                                                     @Qualifier("IngPisObjectMapper") final ObjectMapper objectMapper,
                                                     final MeterRegistry meterRegistry,
                                                     final Clock clock) {
        HttpClientFactory httpClientFactory = new HttpClientFactory(objectMapper, meterRegistry, properties);
        IngSigningUtil ingSigningUtil = new IngSigningUtil();
        DefaultPisHttpClientErrorHandler httpErrorHandler = new DefaultPisHttpClientErrorHandler();
        String providerIdentifier = IngFrProperties.PROVIDER_IDENTIFIER;
        clock.withZone(ZoneId.of("Europe/Paris"));
        Supplier<String> externalTracingIdSupplier = ExternalTracingUtil::createLastExternalTraceId;

        DefaultPisAccessMeansProvider accessMeansProvider = new DefaultPisAccessMeansProvider(
                httpClientFactory,
                httpErrorHandler,
                properties,
                providerIdentifier,
                new DefaultAuthorizationHeadersProvider(ingSigningUtil, properties, clock)
        );

        DefaultRawBankPaymentStatusMapper rawBankPaymentStatusMapper = new DefaultRawBankPaymentStatusMapper(objectMapper);

        var initiatePaymentExecutionContextAdapter = SepaInitiatePaymentExecutionContextAdapterBuilder
                .<SepaCreditTransfer, InitiatePaymentResponse, DefaultInitiatePaymentPreExecutionResult>builder(
                        new DefaultInitiatePaymentStatusesExtractor(new DefaultCommonPaymentStatusMapper()),
                        new DefaultPaymentAuthorizationUrlExtractor(),
                        new CommonProviderStateExtractor<>(objectMapper, new DefaultInitiatePaymentPaymentIdExtractor()),
                        objectMapper,
                        clock,
                        InitiatePaymentResponse.class
                )
                .withPreExecutionResultMapper(new DefaultInitiatePaymentPreExecutionResultMapper(accessMeansProvider, IngItProperties.PROVIDER_IDENTIFIER, clock))
                .withHttpHeadersProvider(new DefaultInitiatePaymentHttpHeadersProvider(new DefaultCommonHttpHeadersProvider(ingSigningUtil, clock, externalTracingIdSupplier), objectMapper))
                .withHttpRequestBodyProvider(new DefaultInitiatePaymentHttpRequestBodyProvider(new DefaultInitiatePaymentHttpRequestBodyMapper()))
                .withHttpRequestInvoker(new DefaultInitiatePaymentHttpRequestInvoker(httpClientFactory, httpErrorHandler, providerIdentifier))
                .withResponseBodyValidator(new DefaultInitiatePaymentResponseBodyValidator())
                .build(rawBankPaymentStatusMapper);

        var submitPaymentPaymentIdExtractor = new DefaultSubmitPaymentPaymentIdExtractor();
        var submitPaymentProviderStateExtractor = new CommonProviderStateExtractor<>(objectMapper, submitPaymentPaymentIdExtractor);
        var submitPaymentExecutionContextAdapter = SepaSubmitPaymentExecutionContextAdapterBuilder
                .<Void, PaymentStatusResponse, DefaultSubmitPaymentPreExecutionResult>builder(
                        new DefaultSubmitPaymentStatusesExtractor(new DefaultCommonPaymentStatusMapper()),
                        submitPaymentPaymentIdExtractor,
                        submitPaymentProviderStateExtractor,
                        objectMapper,
                        clock,
                        PaymentStatusResponse.class
                )
                .withPreExecutionResultMapper(new DefaultSubmitPaymentPreExecutionResultMapper(accessMeansProvider, objectMapper, providerIdentifier, clock))
                .withHttpHeadersProvider(new DefaultSubmitPaymentHttpHeadersProvider(new DefaultCommonHttpHeadersProvider(ingSigningUtil, clock, externalTracingIdSupplier)))
                .withHttpRequestInvoker(new DefaultSubmitPaymentHttpRequestInvoker(httpClientFactory, httpErrorHandler, providerIdentifier))
                .withResponseBodyValidator(new DefaultSubmitPaymentResponseBodyValidator())
                .withGetStatusAsSubmitStep()
                .build(rawBankPaymentStatusMapper);

        var statusPaymentExecutionContextAdapter = SepaStatusPaymentExecutionContextAdapterBuilder
                .<Void, PaymentStatusResponse, DefaultSubmitPaymentPreExecutionResult>builder(
                        new DefaultSubmitPaymentStatusesExtractor(new DefaultCommonPaymentStatusMapper()),
                        new DefaultSubmitPaymentPaymentIdExtractor(),
                        submitPaymentProviderStateExtractor,
                        objectMapper,
                        clock,
                        PaymentStatusResponse.class
                )
                .withPreExecutionResultMapper(new DefaultStatusPaymentPreExecutionResultMapper(accessMeansProvider, providerIdentifier, objectMapper, clock))
                .withHttpHeadersProvider(new DefaultSubmitPaymentHttpHeadersProvider(new DefaultCommonHttpHeadersProvider(ingSigningUtil, clock, externalTracingIdSupplier)))
                .withHttpRequestInvoker(new DefaultStatusPaymentHttpRequestInvoker(httpClientFactory, httpErrorHandler, providerIdentifier))
                .withResponseBodyValidator(new DefaultSubmitPaymentResponseBodyValidator())
                .build(rawBankPaymentStatusMapper);

        return new IngPaymentProviderV2(
                providerIdentifier,
                IngFrProperties.PROVIDER_IDENTIFIER_DISPLAY_NAME,
                ProviderVersion.VERSION_2,
                initiatePaymentExecutionContextAdapter,
                submitPaymentExecutionContextAdapter,
                statusPaymentExecutionContextAdapter);
    }
}
