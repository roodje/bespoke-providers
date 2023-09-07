package com.yolt.providers.stet.bnpparibasgroup.common.pec;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.initiate.SepaInitiateSinglePaymentExecutionContextAdapter;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.initiate.SepaInitiateSinglePaymentExecutionContextAdapterBuilder;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.status.SepaStatusPaymentExecutionContextAdapter;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.status.SepaStatusPaymentExecutionContextAdapterBuilder;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.submit.SepaSubmitPaymentExecutionContextAdapter;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.submit.SepaSubmitPaymentExecutionContextAdapterBuilder;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentAuthorizationUrlExtractor;
import com.yolt.providers.common.rest.http.HttpErrorHandler;
import com.yolt.providers.stet.bnpparibasgroup.BnpParibasGroupPaymentProvider;
import com.yolt.providers.stet.generic.auth.AuthenticationMeansSupplier;
import com.yolt.providers.stet.generic.config.DefaultProperties;
import com.yolt.providers.stet.generic.domain.ProviderIdentification;
import com.yolt.providers.stet.generic.domain.Scope;
import com.yolt.providers.stet.generic.dto.payment.StetPaymentConfirmationRequestDTO;
import com.yolt.providers.stet.generic.dto.payment.StetPaymentInitiationRequestDTO;
import com.yolt.providers.stet.generic.dto.payment.response.StetPaymentInitiationResponseDTO;
import com.yolt.providers.stet.generic.dto.payment.response.StetPaymentStatusResponseDTO;
import com.yolt.providers.stet.generic.http.client.HttpClientFactory;
import com.yolt.providers.stet.generic.mapper.DateTimeSupplier;
import com.yolt.providers.stet.generic.mapper.providerstate.ProviderStateMapper;
import com.yolt.providers.stet.generic.service.pec.authorization.token.*;
import com.yolt.providers.stet.generic.service.pec.common.StetPaymentHttpHeadersFactory;
import com.yolt.providers.stet.generic.service.pec.common.StetPaymentProviderStateExtractor;
import com.yolt.providers.stet.generic.service.pec.common.StetRawBankPaymentStatusMapper;
import com.yolt.providers.stet.generic.service.pec.confirmation.StetConfirmationPaymentPreExecutionResultMapper;
import com.yolt.providers.stet.generic.service.pec.confirmation.StetConfirmationPreExecutionResult;
import com.yolt.providers.stet.generic.service.pec.confirmation.status.*;
import com.yolt.providers.stet.generic.service.pec.initiate.*;
import lombok.RequiredArgsConstructor;

import java.time.Clock;

@RequiredArgsConstructor
public class BnpParibasGroupGenericPaymentProviderFactory {

    private final ProviderIdentification providerIdentification;
    private final PaymentAuthorizationUrlExtractor<StetPaymentInitiationResponseDTO, StetInitiatePreExecutionResult> authorizationUrlExtractor;
    private final StetPaymentHttpHeadersFactory httpHeadersFactory;
    private final HttpClientFactory httpClientFactory;
    private final HttpErrorHandler httpErrorHandler;
    private final AuthenticationMeansSupplier authMeansSupplier;
    private final DateTimeSupplier dateTimeSupplier;
    private final ProviderStateMapper providerStateMapper;
    private final DefaultProperties properties;
    private final ConsentValidityRules consentValidityRules;
    private final Scope paymentScope;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public BnpParibasGroupPaymentProvider createPaymentProvider() {
        var rawBankPaymentStatusMapper = new StetRawBankPaymentStatusMapper(objectMapper);

        var tokenHttpRequestInvoker = new StetTokenPaymentHttpRequestInvoker(
                new BnpParibasGroupTokenPaymentHttpRequestBodyProvider(paymentScope),
                httpHeadersFactory,
                httpClientFactory,
                providerIdentification);

        StetTokenPaymentPreExecutionResultMapper stetTokenPaymentPreExecutionResultMapper = new StetTokenPaymentPreExecutionResultMapper(properties);
        var statusPaymentPreExecutionResultMapper = new StetStatusPaymentPreExecutionResultMapper<>(
                authMeansSupplier,
                providerIdentification,
                stetTokenPaymentPreExecutionResultMapper,
                tokenHttpRequestInvoker,
                providerStateMapper,
                () -> "/payment-requests/{paymentId}");

        return new BnpParibasGroupPaymentProvider(
                providerIdentification,
                createInitiatePECAdapter(stetTokenPaymentPreExecutionResultMapper, tokenHttpRequestInvoker),
                createSubmitPECAdapter(statusPaymentPreExecutionResultMapper),
                createStatusPECAdapter(statusPaymentPreExecutionResultMapper, rawBankPaymentStatusMapper),
                authMeansSupplier,
                consentValidityRules);
    }

    private SepaInitiateSinglePaymentExecutionContextAdapter<StetPaymentInitiationRequestDTO, StetPaymentInitiationResponseDTO, StetInitiatePreExecutionResult> createInitiatePECAdapter(SepaTokenPaymentPreExecutionResultMapper<StetTokenPaymentPreExecutionResult> tokenPaymentPreExecutionResultMapper,
                                                                                                                                                                                         SepaTokenPaymentHttpRequestInvoker<StetTokenPaymentPreExecutionResult> tokenHttpRequestInvoker) {
        StetInitiatePaymentPaymentIdExtractor stetInitiatePaymentPaymentIdExtractor = new StetInitiatePaymentPaymentIdExtractor(() -> "i");
        return SepaInitiateSinglePaymentExecutionContextAdapterBuilder.<StetPaymentInitiationRequestDTO, StetPaymentInitiationResponseDTO, StetInitiatePreExecutionResult>builder(
                        new StetInitiatePaymentStatusesExtractor(),
                        authorizationUrlExtractor,
                        new StetPaymentProviderStateExtractor<>(stetInitiatePaymentPaymentIdExtractor, objectMapper),
                        objectMapper,
                        clock,
                        StetPaymentInitiationResponseDTO.class)
                .withPreExecutionResultMapper(new StetInitiateSinglePaymentPreExecutionResultMapperV2<>(
                        authMeansSupplier,
                        providerIdentification,
                        tokenPaymentPreExecutionResultMapper,
                        tokenHttpRequestInvoker,
                        () -> "/payment-requests"))
                .withHttpRequestBodyProvider(new BnpParibasGroupInitiatePaymentHttpRequestBodyProvider(clock, dateTimeSupplier))
                .withHttpHeadersProvider(new StetInitiatePaymentHttpHeadersProvider(httpHeadersFactory))
                .withHttpRequestInvoker(new StetInitiatePaymentHttpRequestInvoker(httpClientFactory, properties, providerIdentification, httpErrorHandler))
                .withResponseBodyValidator(new StetInitiatePaymentResponseBodyValidator(stetInitiatePaymentPaymentIdExtractor))
                .build(new StetRawBankPaymentStatusMapper(objectMapper));
    }

    private SepaSubmitPaymentExecutionContextAdapter<StetPaymentConfirmationRequestDTO, StetPaymentStatusResponseDTO, StetConfirmationPreExecutionResult> createSubmitPECAdapter(StetStatusPaymentPreExecutionResultMapper<StetTokenPaymentPreExecutionResult> statusPaymentPreExecutionResultMapper) {
        var statusPaymentIdExtractor = new StetStatusPaymentPaymentIdExtractor();
        return SepaSubmitPaymentExecutionContextAdapterBuilder.<StetPaymentConfirmationRequestDTO, StetPaymentStatusResponseDTO, StetConfirmationPreExecutionResult>builder(
                        new StetStatusPaymentStatusesExtractor(),
                        statusPaymentIdExtractor,
                        new StetPaymentProviderStateExtractor<>(statusPaymentIdExtractor, objectMapper),
                        objectMapper,
                        clock,
                        StetPaymentStatusResponseDTO.class)
                .withPreExecutionResultMapper(new StetConfirmationPaymentPreExecutionResultMapper<>(statusPaymentPreExecutionResultMapper, providerStateMapper))
                .withHttpHeadersProvider(new StetStatusPaymentHttpHeadersProvider(httpHeadersFactory))
                .withHttpRequestInvoker(new StetStatusPaymentHttpRequestInvoker(httpClientFactory, providerIdentification, httpErrorHandler, properties))
                .withResponseBodyValidator(new StetStatusResponseBodyValidator())
                .withGetStatusAsSubmitStep()
                .build(new StetRawBankPaymentStatusMapper(objectMapper));
    }

    private SepaStatusPaymentExecutionContextAdapter<StetPaymentConfirmationRequestDTO, StetPaymentStatusResponseDTO, StetConfirmationPreExecutionResult> createStatusPECAdapter(StetStatusPaymentPreExecutionResultMapper<StetTokenPaymentPreExecutionResult> statusPaymentPreExecutionResultMapper,
                                                                                                                                                                                 StetRawBankPaymentStatusMapper rawBankPaymentStatusMapper) {
        var statusPaymentIdExtractor = new StetStatusPaymentPaymentIdExtractor();
        return SepaStatusPaymentExecutionContextAdapterBuilder.<StetPaymentConfirmationRequestDTO, StetPaymentStatusResponseDTO, StetConfirmationPreExecutionResult>builder(
                        new StetStatusPaymentStatusesExtractor(),
                        statusPaymentIdExtractor,
                        new StetPaymentProviderStateExtractor<>(statusPaymentIdExtractor, objectMapper),
                        objectMapper,
                        clock,
                        StetPaymentStatusResponseDTO.class)
                .withPreExecutionResultMapper(statusPaymentPreExecutionResultMapper)
                .withHttpHeadersProvider(new StetStatusPaymentHttpHeadersProvider(httpHeadersFactory))
                .withHttpRequestInvoker(new StetStatusPaymentHttpRequestInvoker(httpClientFactory, providerIdentification, httpErrorHandler, properties))
                .withResponseBodyValidator(new StetStatusResponseBodyValidator())
                .build(rawBankPaymentStatusMapper);
    }
}
