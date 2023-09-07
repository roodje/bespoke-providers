package com.yolt.providers.bunq.beanconfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.bunq.common.BunqPaymentProvider;
import com.yolt.providers.bunq.common.configuration.BunqProperties;
import com.yolt.providers.bunq.common.http.BunqHttpHeaderProducer;
import com.yolt.providers.bunq.common.http.BunqPisHttpClientFactory;
import com.yolt.providers.bunq.common.model.PaymentServiceProviderDraftPaymentRequest;
import com.yolt.providers.bunq.common.model.PaymentServiceProviderDraftPaymentResponse;
import com.yolt.providers.bunq.common.model.PaymentServiceProviderDraftPaymentStatusResponse;
import com.yolt.providers.bunq.common.pis.pec.DefaultEndpointUrlProvider;
import com.yolt.providers.bunq.common.pis.pec.DefaultRawPaymentStatusMapper;
import com.yolt.providers.bunq.common.pis.pec.initiate.*;
import com.yolt.providers.bunq.common.pis.pec.session.Psd2SessionService;
import com.yolt.providers.bunq.common.pis.pec.status.DefaultStatusPaymentPreExecutionResultMapper;
import com.yolt.providers.bunq.common.pis.pec.submit.DefaultSubmitPaymentPreExecutionResultMapper;
import com.yolt.providers.bunq.common.pis.pec.submitandstatus.*;
import com.yolt.providers.bunq.common.service.authorization.BunqAuthorizationServiceV5;
import com.yolt.providers.common.pis.common.PaymentType;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.initiate.SepaInitiateSinglePaymentExecutionContextAdapter;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.initiate.SepaInitiateSinglePaymentExecutionContextAdapterBuilder;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.status.SepaStatusPaymentExecutionContextAdapter;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.status.SepaStatusPaymentExecutionContextAdapterBuilder;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.submit.SepaSubmitPaymentExecutionContextAdapter;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.submit.SepaSubmitPaymentExecutionContextAdapterBuilder;
import com.yolt.providers.common.versioning.ProviderVersion;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class BunqPisBeanConfig {

    @Bean("BunqPaymentProviderV1")
    public BunqPaymentProvider getBunqPaymentProviderV1(MeterRegistry meterRegistry,
                                                        @Qualifier("BunqObjectMapper") ObjectMapper objectMapper,
                                                        BunqProperties properties,
                                                        Clock clock) {

        var endpointUrlProvider = new DefaultEndpointUrlProvider(properties);
        var httpHeaderProducer = new BunqHttpHeaderProducer(objectMapper);
        var authorizationService = new BunqAuthorizationServiceV5(properties);

        return new BunqPaymentProvider(BunqDetailsProvider.BUNQ_PROVIDER_IDENTIFIER,
                BunqDetailsProvider.BUNQ_PROVIDER_DISPLAY_NAME,
                ProviderVersion.VERSION_1,
                createInitiateSinglePaymentAdapter(endpointUrlProvider, httpHeaderProducer, authorizationService, properties, objectMapper, clock, meterRegistry),
                createSubmitSinglePaymentAdapter(endpointUrlProvider, httpHeaderProducer, properties, objectMapper, clock, meterRegistry),
                createStatusSinglePaymentAdapter(endpointUrlProvider, httpHeaderProducer, properties, objectMapper, clock, meterRegistry));
    }

    private SepaInitiateSinglePaymentExecutionContextAdapter<PaymentServiceProviderDraftPaymentRequest, PaymentServiceProviderDraftPaymentResponse, DefaultInitiatePaymentPreExecutionResult> createInitiateSinglePaymentAdapter(
            DefaultEndpointUrlProvider endpointUrlProvider,
            BunqHttpHeaderProducer httpHeaderProducer,
            BunqAuthorizationServiceV5 authorizationService,
            BunqProperties properties,
            ObjectMapper objectMapper,
            Clock clock,
            MeterRegistry meterRegistry) {
        return SepaInitiateSinglePaymentExecutionContextAdapterBuilder
                .<PaymentServiceProviderDraftPaymentRequest, PaymentServiceProviderDraftPaymentResponse, DefaultInitiatePaymentPreExecutionResult>builder(
                        new DefaultInitiatePaymentStatusesExtractor(),
                        new DefaultInitiatePaymentAuthorizationUrlExtractor(authorizationService),
                        new DefaultInitiatePaymentProviderStateExtractor(PaymentType.SINGLE, objectMapper),
                        objectMapper,
                        clock,
                        PaymentServiceProviderDraftPaymentResponse.class
                ).withPreExecutionResultMapper(new DefaultInitiatePaymentPreExecutionResultMapper(
                        new BunqPisHttpClientFactory(objectMapper, meterRegistry, properties),
                        new Psd2SessionService(httpHeaderProducer, endpointUrlProvider, properties),
                        clock))
                .withHttpRequestBodyProvider(new DefaultInitiatePaymentRequestBodyProvider())
                .withHttpHeadersProvider(new DefaultInitiatePaymentHttpHeadersProvider(endpointUrlProvider, httpHeaderProducer))
                .withHttpRequestInvoker(new DefaultInitiatePaymentHttpRequestInvoker(endpointUrlProvider))
                .withResponseBodyValidator(new DefaultInitiatePaymentResponseBodyValidator())
                .build(new DefaultRawPaymentStatusMapper(objectMapper));
    }

    private SepaSubmitPaymentExecutionContextAdapter<Void, PaymentServiceProviderDraftPaymentStatusResponse, DefaultSubmitAndStatusPaymentPreExecutionResult> createSubmitSinglePaymentAdapter(
            DefaultEndpointUrlProvider endpointUrlProvider,
            BunqHttpHeaderProducer httpHeaderProducer,
            BunqProperties properties,
            ObjectMapper objectMapper,
            Clock clock,
            MeterRegistry meterRegistry) {
        return SepaSubmitPaymentExecutionContextAdapterBuilder
                .<Void, PaymentServiceProviderDraftPaymentStatusResponse, DefaultSubmitAndStatusPaymentPreExecutionResult>builder(
                        new DefaultSubmitAndStatusPaymentStatusesExtractor(),
                        new DefaultSubmitAndStatusPaymentIdExtractor(),
                        new DefaultSubmitAndStatusPaymentProviderStateExtractor(PaymentType.SINGLE, objectMapper),
                        objectMapper,
                        clock,
                        PaymentServiceProviderDraftPaymentStatusResponse.class
                )
                .withPreExecutionResultMapper(new DefaultSubmitPaymentPreExecutionResultMapper(
                        new BunqPisHttpClientFactory(objectMapper, meterRegistry, properties),
                        objectMapper,
                        properties,
                        new Psd2SessionService(httpHeaderProducer, endpointUrlProvider, properties),
                        clock))
                .withHttpHeadersProvider(new DefaultSubmitAndStatusPaymentHttpHeadersProvider(endpointUrlProvider, httpHeaderProducer))
                .withHttpRequestInvoker(new DefaultSubmitAndStatusPaymentHttpRequestInvoker(endpointUrlProvider))
                .withResponseBodyValidator(new DefaultSubmitAndStatusPaymentResponseBodyValidator())
                .build(new DefaultRawPaymentStatusMapper(objectMapper));
    }

    private SepaStatusPaymentExecutionContextAdapter<Void, PaymentServiceProviderDraftPaymentStatusResponse, DefaultSubmitAndStatusPaymentPreExecutionResult> createStatusSinglePaymentAdapter(
            DefaultEndpointUrlProvider endpointUrlProvider,
            BunqHttpHeaderProducer httpHeaderProducer,
            BunqProperties properties,
            ObjectMapper objectMapper,
            Clock clock,
            MeterRegistry meterRegistry) {
        return SepaStatusPaymentExecutionContextAdapterBuilder
                .<Void, PaymentServiceProviderDraftPaymentStatusResponse, DefaultSubmitAndStatusPaymentPreExecutionResult>builder(
                        new DefaultSubmitAndStatusPaymentStatusesExtractor(),
                        new DefaultSubmitAndStatusPaymentIdExtractor(),
                        new DefaultSubmitAndStatusPaymentProviderStateExtractor(PaymentType.SINGLE, objectMapper),
                        objectMapper,
                        clock,
                        PaymentServiceProviderDraftPaymentStatusResponse.class
                )
                .withPreExecutionResultMapper(new DefaultStatusPaymentPreExecutionResultMapper(
                        new BunqPisHttpClientFactory(objectMapper, meterRegistry, properties),
                        new Psd2SessionService(httpHeaderProducer, endpointUrlProvider, properties),
                        objectMapper,
                        clock))
                .withHttpHeadersProvider(new DefaultSubmitAndStatusPaymentHttpHeadersProvider(endpointUrlProvider, httpHeaderProducer))
                .withHttpRequestInvoker(new DefaultSubmitAndStatusPaymentHttpRequestInvoker(endpointUrlProvider))
                .withResponseBodyValidator(new DefaultSubmitAndStatusPaymentResponseBodyValidator())
                .build(new DefaultRawPaymentStatusMapper(objectMapper));
    }
}
