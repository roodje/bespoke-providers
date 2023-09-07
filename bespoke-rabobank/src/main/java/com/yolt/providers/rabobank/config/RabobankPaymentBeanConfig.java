package com.yolt.providers.rabobank.config;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.initiate.SepaInitiatePaymentExecutionContextAdapter;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.initiate.SepaInitiatePaymentExecutionContextAdapterBuilder;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.status.SepaStatusPaymentExecutionContextAdapter;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.status.SepaStatusPaymentExecutionContextAdapterBuilder;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.submit.SepaSubmitPaymentExecutionContextAdapter;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.submit.SepaSubmitPaymentExecutionContextAdapterBuilder;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.rabobank.dto.external.InitiatedTransactionResponse;
import com.yolt.providers.rabobank.dto.external.SepaCreditTransfer;
import com.yolt.providers.rabobank.dto.external.StatusResponse;
import com.yolt.providers.rabobank.pis.RabobankPaymentProvider;
import com.yolt.providers.rabobank.pis.pec.*;
import com.yolt.providers.rabobank.pis.pec.initiate.*;
import com.yolt.providers.rabobank.pis.pec.status.RabobankSepaStatusPaymentPreExecutionResultMapper;
import com.yolt.providers.rabobank.pis.pec.submit.*;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class RabobankPaymentBeanConfig {

    private static final String PROVIDER_DISPLAY_NAME = "Rabobank";
    private static final String PROVIDER_IDENTIFIER = "RABOBANK";

    @Bean
    public RabobankPaymentProvider getRabobankPaymentProvider(RabobankProperties properties,
                                                              MeterRegistry meterRegistry) {
        ObjectMapper objectMapper = getRabobankObjectMapper();
        Clock clock = getClock();
        return new RabobankPaymentProvider(createSepaInitiatePaymentExecutionContextAdapter(properties, meterRegistry, objectMapper, clock),
                createSepaSubmitPaymentExecutionContextAdapter(properties, meterRegistry, objectMapper, clock),
                createSepaStatusPaymentExecutionContextAdapter(properties, meterRegistry, objectMapper, clock),
                PROVIDER_IDENTIFIER,
                PROVIDER_DISPLAY_NAME,
                ProviderVersion.VERSION_1);
    }

    SepaInitiatePaymentExecutionContextAdapter<SepaCreditTransfer, InitiatedTransactionResponse, RabobankSepaInitiatePreExecutionResult> createSepaInitiatePaymentExecutionContextAdapter(RabobankProperties properties,
                                                                                                                                                                                          MeterRegistry meterRegistry,
                                                                                                                                                                                          ObjectMapper objectMapper,
                                                                                                                                                                                          Clock clock) {
        RabobankPisHttpClientFactory httpClientFactory = createHttpClientFactory(properties, meterRegistry, objectMapper);
        return SepaInitiatePaymentExecutionContextAdapterBuilder.<SepaCreditTransfer, InitiatedTransactionResponse, RabobankSepaInitiatePreExecutionResult>builder(
                new RabobankSepaInitiatePaymentStatusesExtractor(new RabobankSepaPaymentStatusesMapper()),
                new RabobankSepaInitiatePaymentAuthorizationUrlExtractor(),
                new RabobankCommonProviderStateExtractor<>(new RabobankPaymentProviderStateSerializer(objectMapper), new RabobankSepaInitiatePaymentPaymentIdExtractor()),
                objectMapper,
                clock,
                InitiatedTransactionResponse.class)
                .withPreExecutionResultMapper(new RabobankSepaInitiatePreExecutionResultMapper())
                .withHttpHeadersProvider(new RabobankSepaInitiatePaymentHttpHeadersProvider(objectMapper, new RabobankCommonHttpHeaderProvider(clock), new RabobankPisHeadersSigner()))
                .withHttpRequestBodyProvider(new RabobankSepaInitiatePaymentHttpRequestBodyProvider())
                .withHttpRequestInvoker(new RabobankSepaInitiatePaymentHttpRequestInvoker(httpClientFactory))
                .withResponseBodyValidator(new RabobankSepaInitiatePaymentResponseBodyValidator())
                .build(new RabobankSepaPaymentRawBankStatusMapper(objectMapper));
    }

    private RabobankPisHttpClientFactory createHttpClientFactory(RabobankProperties properties,
                                                                 MeterRegistry meterRegistry,
                                                                 ObjectMapper objectMapper) {
        return new RabobankPisHttpClientFactory(properties, meterRegistry, PROVIDER_DISPLAY_NAME, objectMapper);
    }

    SepaSubmitPaymentExecutionContextAdapter<Void, StatusResponse, RabobankSepaSubmitPaymentPreExecutionResult> createSepaSubmitPaymentExecutionContextAdapter(RabobankProperties properties,
                                                                                                                                                               MeterRegistry meterRegistry,
                                                                                                                                                               ObjectMapper objectMapper,
                                                                                                                                                               Clock clock) {
        RabobankSepaSubmitPaymentPaymentIdExtractor rabobankSepaSubmitPaymentPaymentIdExtractor = new RabobankSepaSubmitPaymentPaymentIdExtractor();
        return SepaSubmitPaymentExecutionContextAdapterBuilder.<Void, StatusResponse, RabobankSepaSubmitPaymentPreExecutionResult>builder(
                new RabobankSepaSubmitPaymentStatusesExtractor(new RabobankSepaPaymentStatusesMapper()),
                rabobankSepaSubmitPaymentPaymentIdExtractor,
                new RabobankCommonProviderStateExtractor<>(new RabobankPaymentProviderStateSerializer(objectMapper), rabobankSepaSubmitPaymentPaymentIdExtractor),
                objectMapper,
                clock,
                StatusResponse.class)
                .withPreExecutionResultMapper(new RaboBankSepaSubmitPaymentPreExecutionResultMapper(new RabobankPaymentProviderStateDeserializer(objectMapper)))
                .withHttpHeadersProvider(new RabobankSepaSumbitPaymentHttpHeadersProvider(new RabobankCommonHttpHeaderProvider(clock), new RabobankPisHeadersSigner()))
                .withHttpRequestInvoker(new RabobankSepaSubmitPaymentRequestInvoker(createHttpClientFactory(properties, meterRegistry, objectMapper)))
                .withResponseBodyValidator(new RabobankSepaSubmitPaymentResponseBodyValidator())
                .withGetStatusAsSubmitStep()
                .build(new RabobankSepaPaymentRawBankStatusMapper(objectMapper));
    }

    SepaStatusPaymentExecutionContextAdapter<Void, StatusResponse, RabobankSepaSubmitPaymentPreExecutionResult> createSepaStatusPaymentExecutionContextAdapter(RabobankProperties properties,
                                                                                                                                                               MeterRegistry meterRegistry,
                                                                                                                                                               ObjectMapper objectMapper,
                                                                                                                                                               Clock clock) {
        RabobankPisHttpClientFactory httpClientFactory = createHttpClientFactory(properties, meterRegistry, objectMapper);
        RabobankSepaSubmitPaymentPaymentIdExtractor rabobankSepaSubmitPaymentPaymentIdExtractor = new RabobankSepaSubmitPaymentPaymentIdExtractor();
        return SepaStatusPaymentExecutionContextAdapterBuilder.<Void, StatusResponse, RabobankSepaSubmitPaymentPreExecutionResult>builder(
                new RabobankSepaSubmitPaymentStatusesExtractor(new RabobankSepaPaymentStatusesMapper()),
                rabobankSepaSubmitPaymentPaymentIdExtractor,
                new RabobankCommonProviderStateExtractor<>(new RabobankPaymentProviderStateSerializer(objectMapper), rabobankSepaSubmitPaymentPaymentIdExtractor),
                objectMapper,
                clock,
                StatusResponse.class)
                .withPreExecutionResultMapper(new RabobankSepaStatusPaymentPreExecutionResultMapper(new RabobankPaymentProviderStateDeserializer(objectMapper)))
                .withHttpHeadersProvider(new RabobankSepaSumbitPaymentHttpHeadersProvider(new RabobankCommonHttpHeaderProvider(clock), new RabobankPisHeadersSigner()))
                .withHttpRequestInvoker(new RabobankSepaSubmitPaymentRequestInvoker(httpClientFactory))
                .withResponseBodyValidator(new RabobankSepaSubmitPaymentResponseBodyValidator())
                .build(new RabobankSepaPaymentRawBankStatusMapper(objectMapper));
    }

    private Clock getClock() {
        return Clock.system(ZoneId.of("Europe/Amsterdam"));
    }

    @Bean
    @Qualifier("RabobankObjectMapper")
    ObjectMapper getRabobankObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return objectMapper;
    }

}
