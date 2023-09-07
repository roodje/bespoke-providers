package com.yolt.providers.volksbank.common.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.initiate.SepaInitiateSinglePaymentExecutionContextAdapterBuilder;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.status.SepaStatusPaymentExecutionContextAdapterBuilder;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.submit.SepaSubmitPaymentExecutionContextAdapterBuilder;
import com.yolt.providers.volksbank.common.pis.VolksbankSepaPaymentProviderV3;
import com.yolt.providers.volksbank.common.pis.pec.*;
import com.yolt.providers.volksbank.common.pis.pec.initiate.*;
import com.yolt.providers.volksbank.common.pis.pec.status.VolksbankStatusPaymentPreExecutionResultMapper;
import com.yolt.providers.volksbank.common.pis.pec.submit.*;
import com.yolt.providers.volksbank.common.rest.VolksbankHttpClientFactoryV2;
import com.yolt.providers.volksbank.common.rest.VolksbankSepaHttpErrorHandler;
import com.yolt.providers.volksbank.dto.v1_1.InitiatePaymentRequest;
import com.yolt.providers.volksbank.dto.v1_1.InitiatePaymentResponse;
import com.yolt.providers.volksbank.dto.v1_1.PaymentStatus;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
@RequiredArgsConstructor
public class VolksbankBeanConfigV2 {

    @Bean
    @Qualifier("Volksbank")
    public ObjectMapper getVolksbankMapper() {
        var mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    public static VolksbankSepaPaymentProviderV3 createVolksbankSepaPaymentProviderV3(ObjectMapper objectMapper,
                                                                                      MeterRegistry meterRegistry,
                                                                                      VolksbankBaseProperties properties,
                                                                                      ProviderIdentification providerIdentification,
                                                                                      ConsentValidityRules consentValidityRules) {
        var httpClientFactory = new VolksbankHttpClientFactoryV2(objectMapper, meterRegistry, new VolksbankSepaHttpErrorHandler(), properties);
        var clock = Clock.system(ZoneId.of("Europe/Amsterdam"));
        var httpHeadersFactory = new VolksbankPisHttpHeadersFactory();
        var rawBankPaymentStatusMapper = new VolksbankRawBankPaymentStatusMapper(objectMapper);
        var providerStateSerializer = new VolksbankPaymentProviderStateSerializer(objectMapper);
        var initiatePaymentExecutionContextAdapter =
                SepaInitiateSinglePaymentExecutionContextAdapterBuilder.<InitiatePaymentRequest, InitiatePaymentResponse, VolksbankSepaInitiatePreExecutionResult>builder(new VolksbankInitiatePaymentStatusesExtractorV2(),
                                new VolksbankPaymentAuthorizationUrlExtractorV2(properties),
                                new VolksbankPaymentProviderStateExtractor<>(providerStateSerializer, new VolksbankInitiatePaymentPaymentIdExtractorV2()),
                                objectMapper,
                                clock,
                                InitiatePaymentResponse.class
                        ).withPreExecutionResultMapper(new VolksbankInitiatePaymentPreExecutionResultMapperV2(providerIdentification))
                        .withHttpRequestBodyProvider(new VolksbankInitiatePaymentHttpRequestBodyProviderV2())
                        .withHttpHeadersProvider(new VolksbankInitiatePaymentHttpHeadersProviderV2(httpHeadersFactory))
                        .withHttpRequestInvoker(new VolksbankInitiatePaymentHttpRequestInvokerV2(httpClientFactory, providerIdentification))
                        .withResponseBodyValidator(new VolksbankInitiatePaymentResponseBodyValidatorV2())
                        .build(rawBankPaymentStatusMapper);

        var submitPaymentStatusesExtractor = new VolksbankSubmitPaymentStatusesExtractorV2();
        var submitPaymentPaymentIdExtractor = new VolksbankSubmitPaymentPaymentIdExtractorV2();
        var submitPaymentHttpHeadersProvider = new VolksbankSubmitPaymentHttpHeadersProvider(httpHeadersFactory);
        var submitPaymentResponseBodyValidator = new VolksbankSubmitResponseBodyValidatorV2();
        var submitPaymentHttpRequestInvoker = new VolksbankSubmitPaymentHttpRequestInvokerV2(httpClientFactory, providerIdentification);
        var submitPaymentProviderStateExtractor = new VolksbankPaymentProviderStateExtractor<>(providerStateSerializer, submitPaymentPaymentIdExtractor);
        var submitPaymentExecutionContextAdapter =
                SepaSubmitPaymentExecutionContextAdapterBuilder.<Void, PaymentStatus, VolksbankSepaSubmitPreExecutionResult>builder(
                                submitPaymentStatusesExtractor,
                                submitPaymentPaymentIdExtractor,
                                submitPaymentProviderStateExtractor,
                                objectMapper,
                                clock,
                                PaymentStatus.class
                        ).withPreExecutionResultMapper(new VolksbankSubmitPaymentPreExecutionResultMapper(new VolksbankPaymentProviderStateDeserializer(objectMapper),
                                providerIdentification))
                        .withHttpHeadersProvider(submitPaymentHttpHeadersProvider)
                        .withHttpRequestInvoker(submitPaymentHttpRequestInvoker)
                        .withResponseBodyValidator(submitPaymentResponseBodyValidator)
                        .withGetStatusAsSubmitStep()
                        .build(rawBankPaymentStatusMapper);

        var statusPaymentExecutionContextAdapter =
                SepaStatusPaymentExecutionContextAdapterBuilder.<Void, PaymentStatus, VolksbankSepaSubmitPreExecutionResult>builder(
                                submitPaymentStatusesExtractor,
                                submitPaymentPaymentIdExtractor,
                                submitPaymentProviderStateExtractor,
                                objectMapper,
                                clock,
                                PaymentStatus.class
                        ).withPreExecutionResultMapper(new VolksbankStatusPaymentPreExecutionResultMapper(new VolksbankPaymentProviderStateDeserializer(objectMapper),
                                providerIdentification))
                        .withHttpHeadersProvider(submitPaymentHttpHeadersProvider)
                        .withHttpRequestInvoker(submitPaymentHttpRequestInvoker)
                        .withResponseBodyValidator(submitPaymentResponseBodyValidator)
                        .build(rawBankPaymentStatusMapper);

        return new VolksbankSepaPaymentProviderV3(
                initiatePaymentExecutionContextAdapter,
                submitPaymentExecutionContextAdapter,
                statusPaymentExecutionContextAdapter,
                providerIdentification,
                consentValidityRules
        );
    }
}
