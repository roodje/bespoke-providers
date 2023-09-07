package com.yolt.providers.yoltprovider;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.initiate.SepaInitiatePeriodicPaymentExecutionContextAdapter;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.initiate.SepaInitiatePeriodicPaymentExecutionContextAdapterBuilder;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.initiate.SepaInitiateSinglePaymentExecutionContextAdapter;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.initiate.SepaInitiateSinglePaymentExecutionContextAdapterBuilder;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.status.SepaStatusPaymentExecutionContextAdapter;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.status.SepaStatusPaymentExecutionContextAdapterBuilder;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.submit.SepaSubmitPaymentExecutionContextAdapter;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.submit.SepaSubmitPaymentExecutionContextAdapterBuilder;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.uk.initiate.*;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.uk.status.UkStatusPaymentExecutionContextAdapter;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.uk.status.UkStatusPaymentExecutionContextAdapterBuilder;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.uk.submit.UkSubmitPaymentExecutionContextAdapter;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.uk.submit.UkSubmitPaymentExecutionContextAdapterBuilder;
import com.yolt.providers.common.pis.ukdomestic.UkProviderState;
import com.yolt.providers.common.rest.ExternalRestTemplateBuilderFactory;
import com.yolt.providers.yoltprovider.pis.YoltBankPaymentRawBankStatusMapper;
import com.yolt.providers.yoltprovider.pis.YoltBankPaymentRequestBodyValidator;
import com.yolt.providers.yoltprovider.pis.sepa.YoltBankSepaPaymentHttpService;
import com.yolt.providers.yoltprovider.pis.sepa.YoltBankSepaPaymentHttpServiceImpl;
import com.yolt.providers.yoltprovider.pis.sepa.pecadapter.YoltBankSepaPaymentStatusesMapper;
import com.yolt.providers.yoltprovider.pis.sepa.pecadapter.initiate.SepaInitiatePaymentResponse;
import com.yolt.providers.yoltprovider.pis.sepa.pecadapter.initiate.YoltBankSepaInitiateErrorHandler;
import com.yolt.providers.yoltprovider.pis.sepa.pecadapter.initiate.YoltBankSepaInitiatePaymentResponseBodyValidator;
import com.yolt.providers.yoltprovider.pis.sepa.pecadapter.initiate.common.*;
import com.yolt.providers.yoltprovider.pis.sepa.pecadapter.initiate.periodic.YoltBankSepaInitiatePeriodicPaymentPaymentHttpInvoker;
import com.yolt.providers.yoltprovider.pis.sepa.pecadapter.initiate.periodic.YoltBankSepaInitiatePeriodicPaymentPreExecutionResultMapper;
import com.yolt.providers.yoltprovider.pis.sepa.pecadapter.initiate.periodic.YoltBankSepaPeriodicPaymentProviderStateExtractor;
import com.yolt.providers.yoltprovider.pis.sepa.pecadapter.initiate.single.YoltBankSepaInitiateSinglePaymentPaymentHttpInvoker;
import com.yolt.providers.yoltprovider.pis.sepa.pecadapter.initiate.single.YoltBankSepaInitiateSinglePaymentPreExecutionResultMapper;
import com.yolt.providers.yoltprovider.pis.sepa.pecadapter.initiate.single.YoltBankSepaSinglePaymentProviderStateExtractor;
import com.yolt.providers.yoltprovider.pis.sepa.pecadapter.status.YoltBankSepaStatusPaymentPaymentHttpRequestInvoker;
import com.yolt.providers.yoltprovider.pis.sepa.pecadapter.status.YoltBankSepaStatusPaymentPreExecutionResultMapper;
import com.yolt.providers.yoltprovider.pis.sepa.pecadapter.submit.*;
import com.yolt.providers.yoltprovider.pis.ukdomestic.*;
import com.yolt.providers.yoltprovider.pis.ukdomestic.models.OBWriteDomesticConsent1;
import com.yolt.providers.yoltprovider.pis.ukdomestic.models.OBWriteDomesticScheduledConsent1;
import com.yolt.providers.yoltprovider.pis.ukdomestic.models.OBWriteDomesticStandingOrderConsent1;
import com.yolt.providers.yoltprovider.pis.ukdomestic.pecadapter.initiate.YoltBankUkDomesticPaymentResponseBodyValidator;
import com.yolt.providers.yoltprovider.pis.ukdomestic.pecadapter.initiate.YoltBankUkPaymentErrorHandler;
import com.yolt.providers.yoltprovider.pis.ukdomestic.pecadapter.initiate.periodic.*;
import com.yolt.providers.yoltprovider.pis.ukdomestic.pecadapter.initiate.single.*;
import com.yolt.providers.yoltprovider.pis.ukdomestic.pecadapter.initiate.single.scheduled.*;
import com.yolt.providers.yoltprovider.pis.ukdomestic.pecadapter.status.YoltBankUkDomesticStatusPaymentPreExecutionResultMapper;
import com.yolt.providers.yoltprovider.pis.ukdomestic.pecadapter.status.YoltBankUkStatusPaymentHttpHeadersProvider;
import com.yolt.providers.yoltprovider.pis.ukdomestic.pecadapter.status.YoltBankUkStatusPaymentPaymentHttpRequestInvoker;
import com.yolt.providers.yoltprovider.pis.ukdomestic.pecadapter.submit.*;
import com.yolt.providers.yoltprovider.pis.ukdomestic.pecadapter.submit.model.PaymentSubmitResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.time.Clock;
import java.time.format.DateTimeFormatter;

@Configuration
public class YoltBankBeanConfig {

    public static final String YOLT_BANK_OBJECT_MAPPER = "yoltBankObjectMapper";
    private static final int MAX_SIZE_INSTRUCTION_IDENTIFICATION = 30;
    private static final DateTimeFormatter INSTRUCTION_IDENTIFICATION_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    @Bean
    public YoltPaymentProvider createPaymentProvider(final ExternalRestTemplateBuilderFactory externalRestTemplateBuilderFactory,
                                                     final YoltProviderConfigurationProperties properties,
                                                     @Qualifier(YOLT_BANK_OBJECT_MAPPER) ObjectMapper objectMapper,
                                                     Clock clock) {
        YoltBankUkPaymentErrorHandler errorHandler = new YoltBankUkPaymentErrorHandler(new YoltBankPostRequestErrorTranslator());
        YoltBankSepaPaymentHttpService yoltBankSepaPaymentHttpService = new YoltBankSepaPaymentHttpServiceImpl(externalRestTemplateBuilderFactory, properties);
        YoltBankUkDomesticHttpService yoltBankUkDomesticHttpService = new YoltBankUkDomesticHttpServiceImpl(externalRestTemplateBuilderFactory, errorHandler, properties);
        return new YoltPaymentProvider(
                sepaInitiateSinglePaymentExecutionContextAdapter(objectMapper, clock, yoltBankSepaPaymentHttpService),
                sepaInitiatePeriodicPaymentExecutionContextAdapter(objectMapper, clock, yoltBankSepaPaymentHttpService),
                sepaSubmitPaymentExecutionContextAdapter(objectMapper, clock, yoltBankSepaPaymentHttpService),
                sepaStatusPaymentExecutionContextAdapter(objectMapper, clock, yoltBankSepaPaymentHttpService),
                ukInitiateSinglePaymentExecutionContextAdapter(objectMapper, clock, yoltBankUkDomesticHttpService),
                ukInitiateScheduledPaymentExecutionContextAdapter(objectMapper, clock, yoltBankUkDomesticHttpService),
                ukInitiatePeriodicPaymentExecutionContextAdapter(objectMapper, clock, yoltBankUkDomesticHttpService),
                ukSubmitPaymentExecutionContextAdapter(objectMapper, clock, yoltBankUkDomesticHttpService),
                ukStatusPaymentExecutionContextAdapter(objectMapper, clock, yoltBankUkDomesticHttpService)
        );
    }

    @Bean("YoltProviderVersion1")
    public YoltProvider createYoltProvider(YoltProviderConfigurationProperties properties,
                                           Clock clock) {
        YoltProviderAuthorizationService yoltProviderAuthorizationService = new YoltProviderAuthorizationService();
        YoltProviderFetchDataService yoltProviderFetchDataService = new YoltProviderFetchDataService();
        return new YoltProvider(properties, yoltProviderFetchDataService, yoltProviderAuthorizationService, clock);
    }

    @Bean("YoltProviderVersion2")
    public YoltProviderVersion2 createYoltProviderVersion2(YoltProviderConfigurationProperties properties,
                                                           Clock clock) {
        YoltProviderAuthorizationService yoltProviderAuthorizationService = new YoltProviderAuthorizationService();
        YoltProviderFetchDataService yoltProviderFetchDataService = new YoltProviderFetchDataService();
        return new YoltProviderVersion2(properties, yoltProviderFetchDataService, yoltProviderAuthorizationService, clock);
    }

    @Bean(YOLT_BANK_OBJECT_MAPPER)
    public ObjectMapper yoltBankObjectMapper() {
        return new Jackson2ObjectMapperBuilder()
                .createXmlMapper(false)
                .serializationInclusion(JsonInclude.Include.NON_NULL)
                .build();
    }

    private UkInitiateScheduledPaymentExecutionContextAdapter<OBWriteDomesticScheduledConsent1, InitiatePaymentConsentResponse, YoltBankUkInitiateScheduledPaymentPreExecutionResult> ukInitiateScheduledPaymentExecutionContextAdapter(
            ObjectMapper objectMapper,
            Clock clock,
            YoltBankUkDomesticHttpService httpService) {
        InstructionIdentificationProvider instructionIdentificationProvider = new InstructionIdentificationProvider(
                MAX_SIZE_INSTRUCTION_IDENTIFICATION,
                INSTRUCTION_IDENTIFICATION_DATE_TIME_FORMATTER,
                clock);
        return UkInitiateScheduledPaymentExecutionContextAdapterBuilder.<OBWriteDomesticScheduledConsent1, InitiatePaymentConsentResponse, YoltBankUkInitiateScheduledPaymentPreExecutionResult>builder(
                        new YoltBankUkDomesticInitiateScheduledPaymentStatusesExtractor(),
                        new YoltBankUkAuthorizationUrlScheduledPaymentExtractor(objectMapper),
                        new YoltBankUkScheduledPaymentProviderStateExtractor(),
                        objectMapper,
                        clock,
                        InitiatePaymentConsentResponse.class)
                .withPreExecutionResultMapper(new YoltBankUkDomesticInitiateScheduledPaymentPreExecutionResultMapper())
                .withHttpRequestBodyProvider(new YoltBankUkInitiateScheduledPaymentHttpRequestBodyProvider(instructionIdentificationProvider))
                .withHttpHeadersProvider(new YoltBankUkInitiateScheduledPaymentHttpHeadersProvider())
                .withHttpRequestInvoker(new YoltBankUkInitiateScheduledPaymentPaymentHttpRequestInvoker(httpService))
                .withResponseBodyValidator(new YoltBankUkDomesticPaymentResponseBodyValidator())
                .build(new YoltBankPaymentRawBankStatusMapper(objectMapper));
    }

    private UkInitiatePeriodicPaymentExecutionContextAdapter<OBWriteDomesticStandingOrderConsent1, InitiatePaymentConsentResponse, YoltBankUkInitiatePeriodicPaymentPreExecutionResult> ukInitiatePeriodicPaymentExecutionContextAdapter(
            ObjectMapper objectMapper,
            Clock clock,
            YoltBankUkDomesticHttpService httpService) {
        return UkInitiatePeriodicPaymentExecutionContextAdapterBuilder.<OBWriteDomesticStandingOrderConsent1, InitiatePaymentConsentResponse, YoltBankUkInitiatePeriodicPaymentPreExecutionResult>builder(
                        new YoltBankUkDomesticInitiatePeriodicPaymentStatusesExtractor(),
                        new YoltBankUkAuthorizationUrlPeriodicPaymentExtractor(objectMapper),
                        new YoltBankUkPeriodicPaymentProviderStateExtractor(objectMapper),
                        objectMapper,
                        clock,
                        InitiatePaymentConsentResponse.class)
                .withPreExecutionResultMapper(new YoltBankUkDomesticInitiatePeriodicPaymentPreExecutionResultMapper())
                .withHttpRequestBodyProvider(new YoltBankUkInitiatePeriodicPaymentHttpRequestBodyProvider())
                .withHttpHeadersProvider(new YoltBankUkInitiatePeriodicPaymentHttpHeadersProvider())
                .withHttpRequestInvoker(new YoltBankUkInitiatePeriodicPaymentPaymentHttpRequestInvoker(httpService))
                .withResponseBodyValidator(new YoltBankUkDomesticPaymentResponseBodyValidator())
                .build(new YoltBankPaymentRawBankStatusMapper(objectMapper));
    }

    private SepaInitiatePeriodicPaymentExecutionContextAdapter<byte[], SepaInitiatePaymentResponse, YoltBankSepaInitiatePaymentPreExecutionResult> sepaInitiatePeriodicPaymentExecutionContextAdapter(
            ObjectMapper objectMapper,
            Clock clock,
            YoltBankSepaPaymentHttpService httpService) {
        {
            var rawStatusMapper = new YoltBankPaymentRawBankStatusMapper(objectMapper);
            return SepaInitiatePeriodicPaymentExecutionContextAdapterBuilder.<byte[], SepaInitiatePaymentResponse, YoltBankSepaInitiatePaymentPreExecutionResult>builder(
                            new YoltBankSepaInitiatePaymentStatusesExtractor(new YoltBankSepaPaymentStatusesMapper()),
                            new YoltBankSepaPaymentAuthorizationUrlExtractor(),
                            new YoltBankSepaPeriodicPaymentProviderStateExtractor(objectMapper),
                            objectMapper,
                            clock,
                            SepaInitiatePaymentResponse.class)
                    .withPreExecutionResultMapper(new YoltBankSepaInitiatePeriodicPaymentPreExecutionResultMapper())
                    .withHttpRequestBodyProvider(new YoltBankSepaInitiatePaymentHttpRequestBodyProvider(objectMapper))
                    .withHttpHeadersProvider(new YoltBankSepaInitiatePaymentHttpHeadersProvider())
                    .withHttpRequestInvoker(new YoltBankSepaInitiatePeriodicPaymentPaymentHttpInvoker(httpService))
                    .withResponseBodyValidator(new YoltBankSepaInitiatePaymentResponseBodyValidator())
                    .buildWithCustomErrorHandler(new YoltBankSepaInitiateErrorHandler(rawStatusMapper, objectMapper, clock));
        }
    }

    private SepaInitiateSinglePaymentExecutionContextAdapter<byte[], SepaInitiatePaymentResponse, YoltBankSepaInitiatePaymentPreExecutionResult> sepaInitiateSinglePaymentExecutionContextAdapter(
            ObjectMapper objectMapper,
            Clock clock,
            YoltBankSepaPaymentHttpService httpService) {
        var rawStatusMapper = new YoltBankPaymentRawBankStatusMapper(objectMapper);
        return SepaInitiateSinglePaymentExecutionContextAdapterBuilder.<byte[], SepaInitiatePaymentResponse, YoltBankSepaInitiatePaymentPreExecutionResult>builder(
                        new YoltBankSepaInitiatePaymentStatusesExtractor(new YoltBankSepaPaymentStatusesMapper()),
                        new YoltBankSepaPaymentAuthorizationUrlExtractor(),
                        new YoltBankSepaSinglePaymentProviderStateExtractor(objectMapper),
                        objectMapper,
                        clock,
                        SepaInitiatePaymentResponse.class)
                .withPreExecutionResultMapper(new YoltBankSepaInitiateSinglePaymentPreExecutionResultMapper())
                .withHttpRequestBodyProvider(new YoltBankSepaInitiatePaymentHttpRequestBodyProvider(objectMapper))
                .withHttpHeadersProvider(new YoltBankSepaInitiatePaymentHttpHeadersProvider())
                .withHttpRequestInvoker(new YoltBankSepaInitiateSinglePaymentPaymentHttpInvoker(httpService))
                .withResponseBodyValidator(new YoltBankSepaInitiatePaymentResponseBodyValidator())
                .buildWithCustomErrorHandler(new YoltBankSepaInitiateErrorHandler(rawStatusMapper, objectMapper, clock));
    }

    private SepaSubmitPaymentExecutionContextAdapter<Void, SepaPaymentStatusResponse, YoltBankSepaSubmitPreExecutionResult> sepaSubmitPaymentExecutionContextAdapter(
            ObjectMapper objectMapper,
            Clock clock,
            YoltBankSepaPaymentHttpService httpService) {
        var rawStatusMapper = new YoltBankPaymentRawBankStatusMapper(objectMapper);
        return SepaSubmitPaymentExecutionContextAdapterBuilder.<Void, SepaPaymentStatusResponse, YoltBankSepaSubmitPreExecutionResult>builder(
                        new YoltBankSepaSubmitPaymentStatusesExtractor(new YoltBankSepaPaymentStatusesMapper()),
                        new YoltBankSepaSubmitPaymentIdExtractor(),
                        new YoltBankSepaSubmitPaymentProviderStateExtractor(objectMapper),
                        objectMapper,
                        clock,
                        SepaPaymentStatusResponse.class)
                .withPreExecutionResultMapper(new YoltBankSepaSubmitPaymentPreExecutionResultMapper(objectMapper))
                .withHttpHeadersProvider(new YoltBankSepaSubmitPaymentHttpHeadersProvider())
                .withHttpRequestInvoker(new YoltBankSepaSubmitPaymentPaymentHttpRequestInvoker(httpService))
                .withResponseBodyValidator(new YoltBankSepaSubmitPaymentResponseBodyValidator())
                .buildWithCustomErrorHandler(new YoltBankSubmitErrorHandler(rawStatusMapper, objectMapper, clock));
    }

    private SepaStatusPaymentExecutionContextAdapter<Void, SepaPaymentStatusResponse, YoltBankSepaSubmitPreExecutionResult> sepaStatusPaymentExecutionContextAdapter(
            ObjectMapper objectMapper,
            Clock clock,
            YoltBankSepaPaymentHttpService httpService) {
        var rawStatusMapper = new YoltBankPaymentRawBankStatusMapper(objectMapper);
        return SepaStatusPaymentExecutionContextAdapterBuilder.<Void, SepaPaymentStatusResponse, YoltBankSepaSubmitPreExecutionResult>builder(
                        new YoltBankSepaSubmitPaymentStatusesExtractor(new YoltBankSepaPaymentStatusesMapper()),
                        new YoltBankSepaSubmitPaymentIdExtractor(),
                        new YoltBankSepaSubmitPaymentProviderStateExtractor(objectMapper),
                        objectMapper,
                        clock,
                        SepaPaymentStatusResponse.class)
                .withPreExecutionResultMapper(new YoltBankSepaStatusPaymentPreExecutionResultMapper(objectMapper))
                .withHttpHeadersProvider(new YoltBankSepaSubmitPaymentHttpHeadersProvider())
                .withHttpRequestInvoker(new YoltBankSepaStatusPaymentPaymentHttpRequestInvoker(httpService))
                .withResponseBodyValidator(new YoltBankSepaSubmitPaymentResponseBodyValidator())
                .build(rawStatusMapper);
    }

    // UK Domestic

    private UkInitiateSinglePaymentExecutionContextAdapter<OBWriteDomesticConsent1, InitiatePaymentConsentResponse, YoltBankUkInitiateSinglePaymentPreExecutionResult> ukInitiateSinglePaymentExecutionContextAdapter(
            ObjectMapper objectMapper,
            Clock clock,
            YoltBankUkDomesticHttpService httpService) {
        InstructionIdentificationProvider instructionIdentificationProvider = new InstructionIdentificationProvider(
                MAX_SIZE_INSTRUCTION_IDENTIFICATION,
                INSTRUCTION_IDENTIFICATION_DATE_TIME_FORMATTER,
                clock);
        return UkInitiateSinglePaymentExecutionContextAdapterBuilder.<OBWriteDomesticConsent1, InitiatePaymentConsentResponse, YoltBankUkInitiateSinglePaymentPreExecutionResult>builder(
                        new YoltBankUkDomesticInitiateSinglePaymentStatusesExtractor(),
                        new YoltBankUkAuthorizationUrlSinglePaymentExtractor(objectMapper),
                        new YoltBankUkSinglePaymentProviderStateExtractor(),
                        objectMapper,
                        clock,
                        InitiatePaymentConsentResponse.class)
                .withUkDomesticInitiatePaymentPreExecutionResultMapper(new YoltBankUkDomesticInitiateSinglePaymentPreExecutionResultMapper(new YoltBankPaymentRequestBodyValidator()))
                .withHttpRequestBodyProvider(new YoltBankUkInitiateSinglePaymentHttpRequestBodyProvider(instructionIdentificationProvider))
                .withHttpHeadersProvider(new YoltBankUkInitiateSinglePaymentHttpHeadersProvider())
                .withHttpRequestInvoker(new YoltBankUkInitiateSinglePaymentPaymentHttpRequestInvoker(httpService))
                .withResponseBodyValidator(new YoltBankUkDomesticPaymentResponseBodyValidator())
                .build(new YoltBankPaymentRawBankStatusMapper(objectMapper));
    }

    private UkSubmitPaymentExecutionContextAdapter<ConfirmPaymentRequest, PaymentSubmitResponse, YoltBankUkSubmitPreExecutionResult> ukSubmitPaymentExecutionContextAdapter(
            ObjectMapper objectMapper,
            Clock clock,
            YoltBankUkDomesticHttpService httpService) {
        return UkSubmitPaymentExecutionContextAdapterBuilder.<ConfirmPaymentRequest, PaymentSubmitResponse, YoltBankUkSubmitPreExecutionResult>builder(
                        new YotlBankUkDomesticSubmitPaymentStatusesExtractor(new UkDomesticPaymentStatusMapper()),
                        new YoltBankUkSubmitPaymentIdExtractor(),
                        (response, result) -> new UkProviderState(null, null, null),
                        objectMapper,
                        clock,
                        PaymentSubmitResponse.class)
                .withUkDomesticSubmitPreExecutionResultMapper(new YoltBankUkDomesticSubmitPreExecutionResultMapper(objectMapper))
                .withHttpRequestBodyProvider(new YoltBankUkSubmitPaymentHttpRequestBodyProvider())
                .withHttpHeadersProvider(new YoltBankUkSubmitPaymentHttpHeadersProvider())
                .withHttpRequestInvoker(new YoltBankUkSubmitPaymentPaymentHttpRequestInvoker(httpService))
                .withResponseBodyValidator(new YoltBankUkDomesticSubmitPaymentResponseBodyValidator())
                .buildWithCustomErrorHandler(new YoltBankSubmitErrorHandler(new YoltBankPaymentRawBankStatusMapper(objectMapper), objectMapper, clock));
    }

    private UkStatusPaymentExecutionContextAdapter<Void, PaymentSubmitResponse, YoltBankUkSubmitPreExecutionResult> ukStatusPaymentExecutionContextAdapter(
            ObjectMapper objectMapper,
            Clock clock,
            YoltBankUkDomesticHttpService httpService) {
        return UkStatusPaymentExecutionContextAdapterBuilder.<Void, PaymentSubmitResponse, YoltBankUkSubmitPreExecutionResult>builder(
                        new YotlBankUkDomesticSubmitPaymentStatusesExtractor(new UkDomesticPaymentStatusMapper()),
                        new YoltBankUkSubmitPaymentIdExtractor(),
                        (response, result) -> new UkProviderState(null, null, null),
                        objectMapper,
                        clock,
                        PaymentSubmitResponse.class
                ).withUkDomesticStatusPaymentPreExecutionResultMapper(new YoltBankUkDomesticStatusPaymentPreExecutionResultMapper(objectMapper))
                .withHttpHeadersProvider(new YoltBankUkStatusPaymentHttpHeadersProvider())
                .withHttpRequestInvoker(new YoltBankUkStatusPaymentPaymentHttpRequestInvoker(httpService))
                .withResponseBodyValidator(new YoltBankUkDomesticSubmitPaymentResponseBodyValidator())
                .build(new YoltBankPaymentRawBankStatusMapper(objectMapper));
    }
}
