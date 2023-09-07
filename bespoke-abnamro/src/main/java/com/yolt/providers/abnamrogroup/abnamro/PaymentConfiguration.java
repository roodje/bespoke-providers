package com.yolt.providers.abnamrogroup.abnamro;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.abnamro.pis.*;
import com.yolt.providers.abnamrogroup.common.pis.*;
import com.yolt.providers.abnamrogroup.common.pis.pec.*;
import com.yolt.providers.abnamrogroup.common.pis.pec.initiate.*;
import com.yolt.providers.abnamrogroup.common.pis.pec.status.*;
import com.yolt.providers.abnamrogroup.common.pis.pec.submit.*;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.initiate.SepaInitiatePaymentExecutionContextAdapter;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.initiate.SepaInitiatePaymentExecutionContextAdapterBuilder;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.status.SepaStatusPaymentExecutionContextAdapter;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.status.SepaStatusPaymentExecutionContextAdapterBuilder;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.submit.SepaSubmitPaymentExecutionContextAdapter;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.submit.SepaSubmitPaymentExecutionContextAdapterBuilder;
import com.yolt.providers.common.pis.paymentexecutioncontext.exception.PaymentExecutionTechnicalException;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class PaymentConfiguration {

    @Bean("AbnAmroObjectMapper")
    public ObjectMapper getObjectMapper(final Jackson2ObjectMapperBuilder mapperBuilder) {
        ObjectMapper mapper = mapperBuilder.build();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper;
    }

    @Bean
    public AbnAmroPaymentProvider getAbnAmroPaymentProvider(final AbnAmroProperties properties,
                                                            @Qualifier("AbnAmroObjectMapper") final ObjectMapper objectMapper,
                                                            final MeterRegistry meterRegistry) {
        String providerDisplayName = "ABN AMRO";
        AbnAmroHttpClientFactory httpClientFactory = new AbnAmroHttpClientFactory(properties, meterRegistry, providerDisplayName, new AbnAmroPisHttpClientErrorHandler());
        Clock clock = Clock.system(ZoneId.of("Europe/Amsterdam"));
        AbnAmroAuthorizationHttpHeadersProvider authorizationHttpHeadersProvider = new AbnAmroAuthorizationHttpHeadersProvider();
        AbnAmroPaymentCommonHttpHeadersProvider commonHttpHeadersProvider = new AbnAmroPaymentCommonHttpHeadersProvider(new AbnAmroRandomUuidXRequestIdHeaderProvider());
        AbnAmroPisAccessTokenProvider pisAccessTokenProvider = new AbnAmroPisAccessTokenProvider(httpClientFactory, authorizationHttpHeadersProvider);
        AbnAmroPaymentStatusMapper paymentStatusMapper = new AbnAmroPaymentStatusMapper();
        AbnAmroProviderStateSerializer providerStateSerializer = new AbnAmroProviderStateSerializer(objectMapper);
        AbnAmroRawBankPaymentStatusMapper rawBankPaymentStatusMapper = new AbnAmroRawBankPaymentStatusMapper(objectMapper);

        SepaInitiatePaymentExecutionContextAdapter<SepaPayment, InitiatePaymentResponseDTO, AbnAmroInitiatePaymentPreExecutionResult> initiatePaymentExecutionContextAdapter =
                SepaInitiatePaymentExecutionContextAdapterBuilder.<SepaPayment, InitiatePaymentResponseDTO, AbnAmroInitiatePaymentPreExecutionResult>builder(
                        new AbnAmroInitiatePaymentStatusesExtractor(paymentStatusMapper),
                        new AbnAmroPaymentAuthorizationUrlExtractor(properties),
                        new AbnAmroInitiatePaymentProviderStateExtractor(providerStateSerializer),
                        objectMapper,
                        clock,
                        InitiatePaymentResponseDTO.class
                ).withPreExecutionResultMapper(new AbnAmroInitiatePaymentPreExecutionResultMapper(pisAccessTokenProvider))
                        .withHttpRequestBodyProvider(new AbnAmroInitiatePaymentHttpRequestBodyProvider())
                        .withHttpHeadersProvider(new AbnAmroInitiatePaymentHttpHeadersProvider(commonHttpHeadersProvider))
                        .withResponseBodyValidator(new AbnAmroInitiatePaymentResponseBodyValidator())
                        .withHttpRequestInvoker(new AbnAmroInitiatePaymentHttpRequestInvoker(httpClientFactory))
                        .build(rawBankPaymentStatusMapper);

        AbnAmroProviderStateDeserializer providerStateDeserializer = new AbnAmroProviderStateDeserializer(objectMapper);
        AbnAmroTransactionStatusResponseValidator transactionStatusResponseValidator = new AbnAmroTransactionStatusResponseValidator();
        SepaSubmitPaymentExecutionContextAdapter<Void, TransactionStatusResponse, AbnAmroSubmitPaymentPreExecutionResult> submitPaymentExecutionContextAdapter =
                SepaSubmitPaymentExecutionContextAdapterBuilder.<Void, TransactionStatusResponse, AbnAmroSubmitPaymentPreExecutionResult>builder(
                        new AbnAmroStatusesExtractor<>(paymentStatusMapper),
                        new AbnAmroPaymentIdExtractor<>(),
                        new AbnAmroSubmitPaymentProviderStateExtractor(providerStateSerializer, clock),
                        objectMapper,
                        clock,
                        TransactionStatusResponse.class
                ).withPreExecutionResultMapper(new AbnAmroSubmitPaymentPreExecutionResultMapper(pisAccessTokenProvider, new AbnAmroAuthorizationCodeExtractor(), providerStateDeserializer))
                        .withHttpHeadersProvider(new AbnAmroSubmitPaymentHttpHeadersProvider(commonHttpHeadersProvider))
                        .withHttpRequestInvoker(new AbnAmroSubmitPaymentHttpRequestInvoker(httpClientFactory))
                        .withResponseBodyValidator(transactionStatusResponseValidator)
                        .buildWithCustomPreExecutionErrorHandler(new AbnAmroPreExecutionErrorHandler(clock, PaymentExecutionTechnicalException::paymentSubmissionException),
                                rawBankPaymentStatusMapper);

        SepaStatusPaymentExecutionContextAdapter<Void, TransactionStatusResponse, AbnAmroPaymentStatusPreExecutionResult> statusPaymentExecutionContextAdapter = SepaStatusPaymentExecutionContextAdapterBuilder.<Void, TransactionStatusResponse, AbnAmroPaymentStatusPreExecutionResult>builder(
                new AbnAmroStatusesExtractor<>(paymentStatusMapper),
                new AbnAmroPaymentIdExtractor<>(),
                new AbnAmroPaymentStatusProviderStateExtractor(providerStateSerializer),
                objectMapper,
                clock,
                TransactionStatusResponse.class)
                .withPreExecutionResultMapper(new AbnAmroPaymentStatusPreExecutionResultMapper(pisAccessTokenProvider, providerStateDeserializer, clock))
                .withHttpHeadersProvider(new AbnAmroPaymentStatusHttpHeadersProvider(commonHttpHeadersProvider))
                .withHttpRequestInvoker(new AbnAmroPaymentStatusRequestInvoker(httpClientFactory))
                .withResponseBodyValidator(transactionStatusResponseValidator)
                .buildWithCustomPreExecutionErrorHandler(new AbnAmroPreExecutionErrorHandler(clock, PaymentExecutionTechnicalException::statusFailed),
                        rawBankPaymentStatusMapper);

        return new AbnAmroPaymentProvider(
                "ABN_AMRO",
                providerDisplayName,
                initiatePaymentExecutionContextAdapter,
                submitPaymentExecutionContextAdapter,
                statusPaymentExecutionContextAdapter
        );
    }
}
