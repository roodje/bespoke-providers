package com.yolt.providers.knabgroup.knab.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.pis.common.PaymentType;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.initiate.SepaInitiateSinglePaymentExecutionContextAdapter;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.initiate.SepaInitiateSinglePaymentExecutionContextAdapterBuilder;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.status.SepaStatusPaymentExecutionContextAdapter;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.status.SepaStatusPaymentExecutionContextAdapterBuilder;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.submit.SepaSubmitPaymentExecutionContextAdapter;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.submit.SepaSubmitPaymentExecutionContextAdapterBuilder;
import com.yolt.providers.common.rest.tracing.ExternalTracingUtil;
import com.yolt.providers.knabgroup.common.KnabGroupDataProviderV2;
import com.yolt.providers.knabgroup.common.KnabGroupPaymentProvider;
import com.yolt.providers.knabgroup.common.auth.KnabGroupAuthenticationServiceV2;
import com.yolt.providers.knabgroup.common.auth.KnabSigningService;
import com.yolt.providers.knabgroup.common.auth.errorhandle.KnabCreateAccessMeansHttpErrorHandlerV2;
import com.yolt.providers.knabgroup.common.auth.errorhandle.KnabGetLoginInfoHttpErrorHandler;
import com.yolt.providers.knabgroup.common.auth.errorhandle.KnabPaymentAccessTokenErrorHandler;
import com.yolt.providers.knabgroup.common.auth.errorhandle.KnabRefreshAccessMeansHttpErrorHandlerV2;
import com.yolt.providers.knabgroup.common.data.KnabGroupFetchDataServiceV2;
import com.yolt.providers.knabgroup.common.data.KnabGroupMapperServiceV2;
import com.yolt.providers.knabgroup.common.data.errorhandler.KnabFetchDataHttpErrorHandlerV2;
import com.yolt.providers.knabgroup.common.http.KnabGroupHttpClientFactory;
import com.yolt.providers.knabgroup.common.payment.*;
import com.yolt.providers.knabgroup.common.payment.dto.Internal.InitiatePaymentPreExecutionResult;
import com.yolt.providers.knabgroup.common.payment.dto.Internal.StatusPaymentPreExecutionResult;
import com.yolt.providers.knabgroup.common.payment.dto.external.InitiatePaymentRequestBody;
import com.yolt.providers.knabgroup.common.payment.dto.external.InitiatePaymentResponse;
import com.yolt.providers.knabgroup.common.payment.dto.external.StatusPaymentResponse;
import com.yolt.providers.knabgroup.common.payment.initiate.*;
import com.yolt.providers.knabgroup.common.payment.status.*;
import com.yolt.providers.knabgroup.common.payment.submit.DefaultSubmitPaymentPreExecutionResultMapper;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import static com.yolt.providers.common.versioning.ProviderVersion.VERSION_1;
import static com.yolt.providers.common.versioning.ProviderVersion.VERSION_2;

@Configuration
public class KnabBeanConfigV2 {

    private static final String PROVIDER_IDENTIFIER = "KNAB_BANK_NL";
    private static final String PROVIDER_DISPLAY_NAME = "Knab Bank (NL)";
    private Supplier<String> externalTracingIdSupplier = ExternalTracingUtil::createLastExternalTraceId;

    @Bean("KnabGroupObjectMapper")
    public ObjectMapper getObjectMapper() {
        return new ObjectMapper();
    }

    @Bean("KnabDataProviderV2")
    public KnabGroupDataProviderV2 getKnabDataProvider(KnabProperties properties,
                                                       @Qualifier("KnabGroupObjectMapper") ObjectMapper objectMapper,
                                                       Clock clock,
                                                       MeterRegistry meterRegistry) {
        KnabGroupHttpClientFactory httpClientFactory = new KnabGroupHttpClientFactory(
                properties, meterRegistry, PROVIDER_DISPLAY_NAME,
                new KnabGetLoginInfoHttpErrorHandler(),
                new KnabCreateAccessMeansHttpErrorHandlerV2(),
                new KnabRefreshAccessMeansHttpErrorHandlerV2(),
                new KnabFetchDataHttpErrorHandlerV2()
        );
        KnabSigningService signingService = new KnabSigningService(objectMapper);
        KnabGroupAuthenticationServiceV2 authenticationService = getAuthenticationServiceV2(httpClientFactory, properties, signingService, clock);
        KnabGroupFetchDataServiceV2 fetchService = getFetchDataService(httpClientFactory, signingService, clock);
        return new KnabGroupDataProviderV2(
                authenticationService,
                fetchService,
                objectMapper,
                clock,
                PROVIDER_IDENTIFIER,
                PROVIDER_DISPLAY_NAME,
                VERSION_2
        );
    }

    @Bean("KnabPaymentProvider")
    public KnabGroupPaymentProvider getPaymentProvider(KnabProperties properties,
                                                       @Qualifier("KnabGroupObjectMapper") ObjectMapper objectMapper,
                                                       Clock clock,
                                                       MeterRegistry meterRegistry) {
        return new KnabGroupPaymentProvider(
                PROVIDER_IDENTIFIER,
                PROVIDER_DISPLAY_NAME,
                VERSION_1,
                getConsentValidityRules(),
                createInitiateSinglePaymentAdapter(properties, objectMapper, clock, meterRegistry),
                createStatusSinglePaymentAdapter(properties, objectMapper, clock, meterRegistry),
                createSubmitSinglePaymentAdapter(properties, objectMapper, clock, meterRegistry));
    }

    private ConsentValidityRules getConsentValidityRules() {
        Set<String> keywords = new HashSet<>();
        keywords.add("Scan de QR-code met de Knab App om de betaling te bevestigen:");
        return new ConsentValidityRules(keywords);
    }

    private SepaInitiateSinglePaymentExecutionContextAdapter<InitiatePaymentRequestBody, InitiatePaymentResponse, InitiatePaymentPreExecutionResult> createInitiateSinglePaymentAdapter(
            KnabProperties properties,
            ObjectMapper objectMapper,
            Clock clock,
            MeterRegistry meterRegistry) {
        DefaultRawBankPaymentStatusMapper rawBankPaymentStatusMapper = new DefaultRawBankPaymentStatusMapper(objectMapper);
        KnabGroupHttpClientFactory httpClientFactory = new KnabGroupHttpClientFactory(properties, meterRegistry, PROVIDER_DISPLAY_NAME, new KnabPaymentAccessTokenErrorHandler(), null, null, null);
        return SepaInitiateSinglePaymentExecutionContextAdapterBuilder
                .<InitiatePaymentRequestBody, InitiatePaymentResponse, InitiatePaymentPreExecutionResult>builder(
                        new DefaultInitiatePaymentStatusesExtractor(new DefaultPaymentStatusMapper()),
                        new DefaultInitiatePaymentAuthorizationUrlExtractor(),
                        new ProviderStateExtractor<>(objectMapper, (response, b) -> response.getPaymentId(), (a, b) -> PaymentType.SINGLE),
                        objectMapper,
                        clock,
                        InitiatePaymentResponse.class
                )
                .withPreExecutionResultMapper(new DefaultInitiatePaymentPreExecutionResultMapper(
                        PROVIDER_IDENTIFIER,
                        new DefaultPisAccessTokenProvider(httpClientFactory)))
                .withHttpHeadersProvider(new DefaultInitiatePaymentExecutionHttpHeadersProvider(new DefaultCommonPaymentHttpHeadersProvider(externalTracingIdSupplier, new KnabSigningService(objectMapper)), objectMapper))
                .withHttpRequestBodyProvider(new DefaultInitiatePaymentHttpRequestBodyProvider())
                .withHttpRequestInvoker(new DefaultInitiatePaymentHttpRequestInvoker(httpClientFactory, new DefaultPisHttpClientErrorHandler(), PaymentType.SINGLE, new PaymentEndpointResolver()))
                .withResponseBodyValidator(new DefaultInitiatePaymentResponseBodyValidator())
                .build(rawBankPaymentStatusMapper);
    }

    private SepaStatusPaymentExecutionContextAdapter<Void, StatusPaymentResponse, StatusPaymentPreExecutionResult> createStatusSinglePaymentAdapter(
            KnabProperties properties,
            ObjectMapper objectMapper,
            Clock clock,
            MeterRegistry meterRegistry) {
        DefaultRawBankPaymentStatusMapper rawBankPaymentStatusMapper = new DefaultRawBankPaymentStatusMapper(objectMapper);
        KnabGroupHttpClientFactory httpClientFactory = new KnabGroupHttpClientFactory(properties, meterRegistry, PROVIDER_DISPLAY_NAME, new KnabPaymentAccessTokenErrorHandler(), null, null, null);
        return SepaStatusPaymentExecutionContextAdapterBuilder
                .<Void, StatusPaymentResponse, StatusPaymentPreExecutionResult>builder(
                        new DefaultStatusPaymentStatusesExtractor(new DefaultPaymentStatusMapper()),
                        new DefaultStatusPaymentIdExtractor(),
                        new ProviderStateExtractor<>(objectMapper, (a, preExecutionResult) -> preExecutionResult.getPaymentId(), (a, b) -> PaymentType.SINGLE),
                        objectMapper,
                        clock,
                        StatusPaymentResponse.class
                )
                .withPreExecutionResultMapper(new DefaultStatusPaymentPreExecutionResultMapper(
                        new DefaultPisAccessTokenProvider(httpClientFactory),
                        PROVIDER_IDENTIFIER,
                        objectMapper))
                .withHttpHeadersProvider(new DefaultStatusPaymentExecutionHttpHeadersProvider(new DefaultCommonPaymentHttpHeadersProvider(externalTracingIdSupplier, new KnabSigningService(objectMapper))))
                .withHttpRequestInvoker(new DefaultStatusPaymentHttpRequestInvoker(httpClientFactory, new DefaultPisHttpClientErrorHandler(), new PaymentEndpointResolver()))
                .withResponseBodyValidator(new DefaultStatusPaymentResponseBodyValidator())
                .build(rawBankPaymentStatusMapper);
    }

    private SepaSubmitPaymentExecutionContextAdapter<Void, StatusPaymentResponse, StatusPaymentPreExecutionResult> createSubmitSinglePaymentAdapter(
            KnabProperties properties,
            ObjectMapper objectMapper,
            Clock clock,
            MeterRegistry meterRegistry) {
        DefaultRawBankPaymentStatusMapper rawBankPaymentStatusMapper = new DefaultRawBankPaymentStatusMapper(objectMapper);
        KnabGroupHttpClientFactory httpClientFactory = new KnabGroupHttpClientFactory(properties, meterRegistry, PROVIDER_DISPLAY_NAME, new KnabPaymentAccessTokenErrorHandler(), null, null, null);
        return SepaSubmitPaymentExecutionContextAdapterBuilder
                .<Void, StatusPaymentResponse, StatusPaymentPreExecutionResult>builder(
                        new DefaultStatusPaymentStatusesExtractor(new DefaultPaymentStatusMapper()),
                        new DefaultStatusPaymentIdExtractor(),
                        new ProviderStateExtractor<>(objectMapper, (a, preExecutionResult) -> preExecutionResult.getPaymentId(), (a, b) -> PaymentType.SINGLE),
                        objectMapper,
                        clock,
                        StatusPaymentResponse.class
                )
                .withPreExecutionResultMapper(new DefaultSubmitPaymentPreExecutionResultMapper(
                        new DefaultPisAccessTokenProvider(httpClientFactory),
                        PROVIDER_IDENTIFIER,
                        objectMapper))
                .withHttpHeadersProvider(new DefaultStatusPaymentExecutionHttpHeadersProvider(new DefaultCommonPaymentHttpHeadersProvider(externalTracingIdSupplier, new KnabSigningService(objectMapper))))
                .withHttpRequestInvoker(new DefaultStatusPaymentHttpRequestInvoker(httpClientFactory, new DefaultPisHttpClientErrorHandler(), new PaymentEndpointResolver()))
                .withResponseBodyValidator(new DefaultStatusPaymentResponseBodyValidator())
                .withGetStatusAsSubmitStep()
                .build(rawBankPaymentStatusMapper);
    }

    private KnabGroupAuthenticationServiceV2 getAuthenticationServiceV2(KnabGroupHttpClientFactory httpClientFactory, KnabProperties properties, KnabSigningService signingService, Clock clock) {
        return new KnabGroupAuthenticationServiceV2(httpClientFactory, signingService, properties, clock);
    }

    private KnabGroupFetchDataServiceV2 getFetchDataService(KnabGroupHttpClientFactory httpClientFactory, KnabSigningService signingService, Clock clock) {
        return new KnabGroupFetchDataServiceV2(httpClientFactory, signingService, new KnabGroupMapperServiceV2(ZoneId.of("Europe/Amsterdam")), clock);
    }

}