package com.yolt.providers.fineco.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.yolt.providers.common.pis.common.PaymentType;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.initiate.SepaInitiateSinglePaymentExecutionContextAdapterBuilder;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.status.SepaStatusPaymentExecutionContextAdapterBuilder;
import com.yolt.providers.common.rest.http.DefaultHttpErrorHandlerV2;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.fineco.FinecoPaymentProvider;
import com.yolt.providers.fineco.dto.PaymentRequest;
import com.yolt.providers.fineco.dto.PaymentResponse;
import com.yolt.providers.fineco.pis.FinecoPaymentStatusMapper;
import com.yolt.providers.fineco.pis.common.FinecoRawBankPaymentStatusMapper;
import com.yolt.providers.fineco.pis.initiate.*;
import com.yolt.providers.fineco.pis.status.*;
import com.yolt.providers.fineco.rest.FinecoHttpClientFactory;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

import static com.yolt.providers.fineco.FinecoDetailsProvider.PROVIDER_KEY;

@Configuration
@RequiredArgsConstructor
public class FinecoBeanConfig {

    @Bean("FinecoObjectMapper")
    public ObjectMapper getFinecoObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return objectMapper;
    }

    @Bean("FinecoPaymentProviderV1")
    public FinecoPaymentProvider getFinecoPaymentProviderV1(@Qualifier("FinecoObjectMapper") ObjectMapper objectMapper,
                                                            Clock clock,
                                                            MeterRegistry meterRegistry,
                                                            FinecoProperties properties) {

        var httpClientFactory = new FinecoHttpClientFactory(objectMapper, meterRegistry, properties);

        var initiatePecAdapter = SepaInitiateSinglePaymentExecutionContextAdapterBuilder
                .<PaymentRequest, PaymentResponse, FinecoInitiatePaymentPreExecutionResult>builder(
                        new FinecoInitiatePaymentStatusesExtractor(new FinecoPaymentStatusMapper()),
                        new FinecoPaymentAuthorizationUrlExtractor(),
                        new FinecoInitiateProviderStateExtractor(objectMapper, PaymentType.SINGLE),
                        objectMapper,
                        clock,
                        PaymentResponse.class
                )
                .withPreExecutionResultMapper(new FinecoInitiatePaymentPreExecutionResultMapper(PROVIDER_KEY))
                .withHttpRequestBodyProvider(new FinecoInitiatePaymentHttpRequestBodyProvider())
                .withHttpRequestInvoker(new FinecoInitiatePaymentHttpRequestInvoker(httpClientFactory, new DefaultHttpErrorHandlerV2(), PROVIDER_KEY, properties))
                .withResponseBodyValidator(new FinecoInitiatePaymentResponseBodyValidator())
                .withHttpHeadersProvider(new FinecoInitiatePaymentHttpHeadersProvider())
                .build(new FinecoRawBankPaymentStatusMapper(objectMapper));

        var statusPecAdapter = SepaStatusPaymentExecutionContextAdapterBuilder
                .<Void, PaymentResponse, FinecoStatusPaymentPreExecutionResult>builder(
                        new FinecoStatusPaymentStatusesExtractor(new FinecoPaymentStatusMapper()),
                        new FinecoStatusPaymentPaymentIdExtractor(),
                        new FinecoStatusProviderStateExtractor(objectMapper),
                        objectMapper,
                        clock,
                        PaymentResponse.class
                )
                .withPreExecutionResultMapper(new FinecoStatusPaymentPreExecutionResultMapper(PROVIDER_KEY, objectMapper))
                .withHttpHeadersProvider(new FinecoStatusPaymentHttpHeadersProvider())
                .withHttpRequestInvoker(new FinecoStatusPaymentHttpRequestInvoker(httpClientFactory, new DefaultHttpErrorHandlerV2(), PROVIDER_KEY, properties))
                .withResponseBodyValidator(new FinecoStatusPaymentResponseBodyValidator())
                .build(new FinecoRawBankPaymentStatusMapper(objectMapper));

        return new FinecoPaymentProvider(initiatePecAdapter, statusPecAdapter, ProviderVersion.VERSION_1);
    }
}
