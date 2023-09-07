package com.yolt.providers.cbiglobe.common.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.yolt.providers.cbiglobe.common.CbiGlobeSepaPaymentProviderV3;
import com.yolt.providers.cbiglobe.common.model.InitiatePaymentRequest;
import com.yolt.providers.cbiglobe.common.pis.pec.CbiGlobePaymentProviderStateExtractor;
import com.yolt.providers.cbiglobe.common.pis.pec.*;
import com.yolt.providers.cbiglobe.common.pis.pec.auth.CbiGlobePaymentAccessTokenProvider;
import com.yolt.providers.cbiglobe.common.pis.pec.auth.CbiGlobePisAuthenticationService;
import com.yolt.providers.cbiglobe.common.pis.pec.initiate.*;
import com.yolt.providers.cbiglobe.common.pis.pec.status.CbiGlobeStatusPaymentPreExecutionResultMapper;
import com.yolt.providers.cbiglobe.common.pis.pec.submit.*;
import com.yolt.providers.cbiglobe.common.rest.CbiGlobePisHttpClientFactory;
import com.yolt.providers.cbiglobe.pis.dto.GetPaymentStatusRequestResponseType;
import com.yolt.providers.cbiglobe.pis.dto.PaymentInitiationRequestResponseType;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.initiate.SepaInitiatePaymentExecutionContextAdapterBuilder;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.status.SepaStatusPaymentExecutionContextAdapterBuilder;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.submit.SepaSubmitPaymentExecutionContextAdapterBuilder;
import com.yolt.providers.common.pis.paymentexecutioncontext.http.PaymentExecutionHttpRequestBodyProvider;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
@RequiredArgsConstructor
public class CbiGlobeConfiguration {

    @Bean("CbiGlobeObjectMapper")
    @Qualifier("CbiGlobe")
    public ObjectMapper getObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    public static CbiGlobeSepaPaymentProviderV3 createCbiGlobeSepaPaymentProviderV3(PaymentExecutionHttpRequestBodyProvider<CbiGlobeSepaInitiatePreExecutionResult, InitiatePaymentRequest> paymentExecutionHttpRequestBodyProvider,
                                                                                    ObjectMapper objectMapper,
                                                                                    MeterRegistry meterRegistry,
                                                                                    CbiGlobeBaseProperties properties,
                                                                                    ProviderIdentification providerIdentification,
                                                                                    ConsentValidityRules consentValidityRules) {
        var httpClientFactory = new CbiGlobePisHttpClientFactory(objectMapper, meterRegistry, properties);
        var clock = Clock.system(ZoneId.of("Europe/Rome"));
        var httpHeadersFactory = new CbiGlobePisHttpHeadersFactory();
        var rawBankPaymentStatusMapper = new CbiGlobeRawBankPaymentStatusMapper(objectMapper);
        var providerStateSerializer = new CbiGlobePaymentProviderStateSerializer(objectMapper);
        var authenticationService = new CbiGlobePisAuthenticationService(httpHeadersFactory, properties, clock);
        var paymentAccessTokenProvider = new CbiGlobePaymentAccessTokenProvider(httpClientFactory, authenticationService, providerIdentification);
        var initiatePaymentExecutionContextAdapter =
                SepaInitiatePaymentExecutionContextAdapterBuilder.<InitiatePaymentRequest, PaymentInitiationRequestResponseType, CbiGlobeSepaInitiatePreExecutionResult>builder(new CbiGlobeInitiatePaymentStatusesExtractor(),
                        new CbiGlobePaymentAuthorizationUrlExtractor(),
                        new CbiGlobePaymentProviderStateExtractor<>(providerStateSerializer, new CbiGlobeInitiatePaymentPaymentIdExtractor()),
                        objectMapper,
                        clock,
                        PaymentInitiationRequestResponseType.class
                ).withPreExecutionResultMapper(new CbiGlobeInitiatePaymentPreExecutionResultMapper(paymentAccessTokenProvider, providerIdentification, properties))
                        .withHttpRequestBodyProvider(paymentExecutionHttpRequestBodyProvider)
                        .withHttpHeadersProvider(new CbiGlobeInitiatePaymentHttpHeadersProvider(httpHeadersFactory, clock))
                        .withHttpRequestInvoker(new CbiGlobeInitiatePaymentHttpRequestInvoker(httpClientFactory, providerIdentification))
                        .withResponseBodyValidator(new CbiGlobeInitiatePaymentResponseBodyValidator())
                        .build(rawBankPaymentStatusMapper);

        var submitPaymentStatusesExtractor = new CbiGlobeSubmitPaymentStatusesExtractor();
        var submitPaymentPaymentIdExtractor = new CbiGlobeSubmitPaymentPaymentIdExtractor();
        var submitPaymentHttpHeadersProvider = new CbiGlobeSubmitPaymentHttpHeadersProvider(httpHeadersFactory, clock);
        var submitPaymentResponseBodyValidator = new CbiGlobeSubmitResponseBodyValidator();
        var submitPaymentHttpRequestInvoker = new CbiGlobeSubmitPaymentHttpRequestInvoker(httpClientFactory, providerIdentification);
        var submitPaymentProviderStateExtractor = new CbiGlobePaymentProviderStateExtractor<>(providerStateSerializer, submitPaymentPaymentIdExtractor);
        var submitPaymentExecutionContextAdapter =
                SepaSubmitPaymentExecutionContextAdapterBuilder.<Void, GetPaymentStatusRequestResponseType, CbiGlobeSepaSubmitPreExecutionResult>builder(
                        submitPaymentStatusesExtractor,
                        submitPaymentPaymentIdExtractor,
                        submitPaymentProviderStateExtractor,
                        objectMapper,
                        clock,
                        GetPaymentStatusRequestResponseType.class
                ).withPreExecutionResultMapper(new CbiGlobeSubmitPaymentPreExecutionResultMapper(paymentAccessTokenProvider,
                        new CbiGlobePaymentProviderStateDeserializer(objectMapper), providerIdentification, properties))
                        .withHttpHeadersProvider(submitPaymentHttpHeadersProvider)
                        .withHttpRequestInvoker(submitPaymentHttpRequestInvoker)
                        .withResponseBodyValidator(submitPaymentResponseBodyValidator)
                        .withGetStatusAsSubmitStep()
                        .build(rawBankPaymentStatusMapper);

        var statusPaymentExecutionContextAdapter =
                SepaStatusPaymentExecutionContextAdapterBuilder.<Void, GetPaymentStatusRequestResponseType, CbiGlobeSepaSubmitPreExecutionResult>builder(
                        submitPaymentStatusesExtractor,
                        submitPaymentPaymentIdExtractor,
                        submitPaymentProviderStateExtractor,
                        objectMapper,
                        clock,
                        GetPaymentStatusRequestResponseType.class
                ).withPreExecutionResultMapper(new CbiGlobeStatusPaymentPreExecutionResultMapper(paymentAccessTokenProvider,
                        new CbiGlobePaymentProviderStateDeserializer(objectMapper), providerIdentification, properties))
                        .withHttpHeadersProvider(submitPaymentHttpHeadersProvider)
                        .withHttpRequestInvoker(submitPaymentHttpRequestInvoker)
                        .withResponseBodyValidator(submitPaymentResponseBodyValidator)
                        .build(rawBankPaymentStatusMapper);

        return new CbiGlobeSepaPaymentProviderV3(
                initiatePaymentExecutionContextAdapter,
                submitPaymentExecutionContextAdapter,
                statusPaymentExecutionContextAdapter,
                providerIdentification,
                consentValidityRules
        );
    }
}
