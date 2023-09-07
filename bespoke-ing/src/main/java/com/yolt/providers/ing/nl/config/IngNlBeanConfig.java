package com.yolt.providers.ing.nl.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.pis.common.PaymentType;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.initiate.SepaInitiatePeriodicPaymentExecutionContextAdapterBuilder;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.initiate.SepaInitiateSinglePaymentExecutionContextAdapterBuilder;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.status.SepaStatusPaymentExecutionContextAdapterBuilder;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.submit.SepaSubmitPaymentExecutionContextAdapterBuilder;
import com.yolt.providers.common.rest.tracing.ExternalTracingUtil;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.ing.common.IngDataProviderV9;
import com.yolt.providers.ing.common.IngPaymentProviderV3;
import com.yolt.providers.ing.common.config.IngObjectMapper;
import com.yolt.providers.ing.common.dto.InitiatePaymentResponse;
import com.yolt.providers.ing.common.dto.PaymentStatusResponse;
import com.yolt.providers.ing.common.dto.SepaCreditTransfer;
import com.yolt.providers.ing.common.exception.DefaultPisHttpClientErrorHandler;
import com.yolt.providers.ing.common.http.HttpClientFactory;
import com.yolt.providers.ing.common.pec.*;
import com.yolt.providers.ing.common.pec.initiate.*;
import com.yolt.providers.ing.common.pec.status.DefaultStatusPaymentHttpRequestInvokerV2;
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
public class IngNlBeanConfig {

    @Bean("IngNlDataProviderV10")
    public IngDataProviderV9 getDataProviderV10(IngNlProperties properties, Clock clock) {
        IngClientAwareRestTemplateService restTemplateService = new IngClientAwareRestTemplateService(properties);
        IngSigningUtil ingSigningUtil = new IngSigningUtil();
        IngFetchDataService fetchDataService = new IngFetchDataServiceV9(new IngDataMapperServiceV6(IngNlProperties.ZONE_ID), restTemplateService, ingSigningUtil, properties, clock);
        IngAuthenticationServiceV3 authenticationService = new IngAuthenticationServiceV3(restTemplateService, ingSigningUtil, properties, IngNlProperties.COUNTRY_CODE, clock);
        return new IngDataProviderV9(
                fetchDataService,
                authenticationService,
                IngObjectMapper.get(),
                IngNlProperties.PROVIDER_IDENTIFIER,
                IngNlProperties.PROVIDER_IDENTIFIER_DISPLAY_NAME,
                ProviderVersion.VERSION_10,
                clock);
    }

    @Bean("IngNlPaymentProviderV3")
    public IngPaymentProviderV3 getPaymentProviderV3(final IngNlProperties properties,
                                                     @Qualifier("IngPisObjectMapper") final ObjectMapper objectMapper,
                                                     final MeterRegistry meterRegistry,
                                                     final Clock clock) {
        HttpClientFactory httpClientFactory = new HttpClientFactory(objectMapper, meterRegistry, properties);
        IngSigningUtil ingSigningUtil = new IngSigningUtil();
        DefaultPisHttpClientErrorHandler httpErrorHandler = new DefaultPisHttpClientErrorHandler();
        String providerIdentifier = IngNlProperties.PROVIDER_IDENTIFIER;
        clock.withZone(ZoneId.of("Europe/Amsterdam"));
        Supplier<String> externalTracingIdSupplier = ExternalTracingUtil::createLastExternalTraceId;

        DefaultPisAccessMeansProvider accessMeansProvider = new DefaultPisAccessMeansProvider(
                httpClientFactory,
                httpErrorHandler,
                properties,
                providerIdentifier,
                new DefaultAuthorizationHeadersProvider(ingSigningUtil, properties, clock)
        );

        DefaultRawBankPaymentStatusMapper rawBankPaymentStatusMapper = new DefaultRawBankPaymentStatusMapper(objectMapper);
        var paymentEndpointResolver = new PaymentEndpointResolver();

        var initiateSinglePaymentExecutionContextAdapter = SepaInitiateSinglePaymentExecutionContextAdapterBuilder
                .<SepaCreditTransfer, InitiatePaymentResponse, DefaultInitiatePaymentPreExecutionResult>builder(
                        new DefaultInitiatePaymentStatusesExtractor(new DefaultCommonPaymentStatusMapper()),
                        new DefaultPaymentAuthorizationUrlExtractor(),
                        new CommonProviderStateExtractorV2<>(objectMapper, new DefaultInitiatePaymentPaymentIdExtractor(), (a, b) -> PaymentType.SINGLE),
                        objectMapper,
                        clock,
                        InitiatePaymentResponse.class
                )
                .withPreExecutionResultMapper(new DefaultInitiateSingleAndScheduledPaymentPreExecutionResultMapper(accessMeansProvider, IngItProperties.PROVIDER_IDENTIFIER, clock))
                .withHttpHeadersProvider(new DefaultInitiatePaymentHttpHeadersProvider(new DefaultCommonHttpHeadersProvider(ingSigningUtil, clock, externalTracingIdSupplier), objectMapper))
                .withHttpRequestBodyProvider(new DefaultInitiatePaymentHttpRequestBodyProvider(new DefaultInitiatePaymentHttpRequestBodyMapper()))
                .withHttpRequestInvoker(new DefaultInitiatePaymentHttpRequestInvoker(httpClientFactory, httpErrorHandler, providerIdentifier))
                .withResponseBodyValidator(new DefaultInitiatePaymentResponseBodyValidator())
                .build(rawBankPaymentStatusMapper);

        var initiateSheduledPaymentExecutionContextAdapter = SepaInitiateSinglePaymentExecutionContextAdapterBuilder
                .<SepaCreditTransfer, InitiatePaymentResponse, DefaultInitiatePaymentPreExecutionResult>builder(
                        new DefaultInitiatePaymentStatusesExtractor(new DefaultCommonPaymentStatusMapper()),
                        new DefaultPaymentAuthorizationUrlExtractor(),
                        new CommonProviderStateExtractorV2<>(objectMapper, new DefaultInitiatePaymentPaymentIdExtractor(), (a, b) -> PaymentType.SCHEDULED),
                        objectMapper,
                        clock,
                        InitiatePaymentResponse.class
                )
                .withPreExecutionResultMapper(new DefaultInitiateSingleAndScheduledPaymentPreExecutionResultMapper(accessMeansProvider, IngItProperties.PROVIDER_IDENTIFIER, clock))
                .withHttpHeadersProvider(new DefaultInitiatePaymentHttpHeadersProvider(new DefaultCommonHttpHeadersProvider(ingSigningUtil, clock, externalTracingIdSupplier), objectMapper))
                .withHttpRequestBodyProvider(new DefaultInitiatePaymentHttpRequestBodyProvider(new DefaultInitiatePaymentHttpRequestBodyMapper()))
                .withHttpRequestInvoker(new DefaultInitiatePaymentHttpRequestInvoker(httpClientFactory, httpErrorHandler, providerIdentifier))
                .withResponseBodyValidator(new DefaultInitiatePaymentResponseBodyValidator())
                .build(rawBankPaymentStatusMapper);

        var initiatePeriodicPaymentExecutionContextAdapter = SepaInitiatePeriodicPaymentExecutionContextAdapterBuilder
                .<SepaCreditTransfer, InitiatePaymentResponse, DefaultInitiatePaymentPreExecutionResult>builder(
                        new DefaultInitiatePaymentStatusesExtractor(new DefaultCommonPaymentStatusMapper()),
                        new DefaultPaymentAuthorizationUrlExtractor(),
                        new CommonProviderStateExtractorV2<>(objectMapper, new DefaultInitiatePaymentPaymentIdExtractor(), (a, b) -> PaymentType.PERIODIC),
                        objectMapper,
                        clock,
                        InitiatePaymentResponse.class
                )
                .withPreExecutionResultMapper(new DefaultInitiatePeriodicPaymentPreExecutionResultMapper(accessMeansProvider, IngItProperties.PROVIDER_IDENTIFIER, clock))
                .withHttpHeadersProvider(new DefaultInitiatePaymentHttpHeadersProviderV2(
                        new DefaultCommonHttpHeadersProvider(ingSigningUtil, clock, externalTracingIdSupplier), objectMapper, PaymentType.PERIODIC, paymentEndpointResolver))
                .withHttpRequestBodyProvider(new DefaultInitiatePeriodicPaymentHttpRequestBodyProvider(new DefaultInitiatePaymentHttpRequestBodyMapper()))
                .withHttpRequestInvoker(new DefaultInitiatePaymentHttpRequestInvokerV2(httpClientFactory, httpErrorHandler, providerIdentifier, PaymentType.PERIODIC, paymentEndpointResolver))
                .withResponseBodyValidator(new DefaultInitiatePaymentResponseBodyValidator())
                .build(rawBankPaymentStatusMapper);

        var submitPaymentPaymentIdExtractor = new DefaultSubmitPaymentPaymentIdExtractor();
        var submitPaymentProviderStateExtractor = new CommonProviderStateExtractorV2<>(objectMapper, submitPaymentPaymentIdExtractor, new DefaultSubmitPaymentTypeExtractor());
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
                .withHttpHeadersProvider(new DefaultSubmitPaymentHttpHeadersProviderV2(new DefaultCommonHttpHeadersProvider(ingSigningUtil, clock, externalTracingIdSupplier), paymentEndpointResolver))
                .withHttpRequestInvoker(new DefaultSubmitPaymentHttpRequestInvokerV2(httpClientFactory, httpErrorHandler, providerIdentifier, paymentEndpointResolver))
                .withResponseBodyValidator(new DefaultSubmitPaymentResponseBodyValidator())
                .withGetStatusAsSubmitStep()
                .build(rawBankPaymentStatusMapper);

        var statusPaymentExecutionContextAdapter = SepaStatusPaymentExecutionContextAdapterBuilder
                .<Void, PaymentStatusResponse, DefaultSubmitPaymentPreExecutionResult>builder(
                        new DefaultSubmitPaymentStatusesExtractor(new DefaultCommonPaymentStatusMapper()),
                        submitPaymentPaymentIdExtractor,
                        submitPaymentProviderStateExtractor,
                        objectMapper,
                        clock,
                        PaymentStatusResponse.class
                )
                .withPreExecutionResultMapper(new DefaultStatusPaymentPreExecutionResultMapper(accessMeansProvider, providerIdentifier, objectMapper, clock))
                .withHttpHeadersProvider(new DefaultSubmitPaymentHttpHeadersProviderV2(new DefaultCommonHttpHeadersProvider(ingSigningUtil, clock, externalTracingIdSupplier), paymentEndpointResolver))
                .withHttpRequestInvoker(new DefaultStatusPaymentHttpRequestInvokerV2(httpClientFactory, httpErrorHandler, providerIdentifier, paymentEndpointResolver))
                .withResponseBodyValidator(new DefaultSubmitPaymentResponseBodyValidator())
                .build(rawBankPaymentStatusMapper);

        return new IngPaymentProviderV3(
                providerIdentifier,
                IngNlProperties.PROVIDER_IDENTIFIER_DISPLAY_NAME,
                ProviderVersion.VERSION_2,
                initiateSinglePaymentExecutionContextAdapter,
                initiateSheduledPaymentExecutionContextAdapter,
                initiatePeriodicPaymentExecutionContextAdapter,
                submitPaymentExecutionContextAdapter,
                statusPaymentExecutionContextAdapter);
    }
}
