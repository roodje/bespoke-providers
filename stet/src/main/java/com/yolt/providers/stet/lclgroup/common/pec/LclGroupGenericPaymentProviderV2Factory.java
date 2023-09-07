package com.yolt.providers.stet.lclgroup.common.pec;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.initiate.SepaInitiateSinglePaymentExecutionContextAdapter;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.initiate.SepaInitiateSinglePaymentExecutionContextAdapterBuilder;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.status.SepaStatusPaymentExecutionContextAdapter;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.status.SepaStatusPaymentExecutionContextAdapterBuilder;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.submit.SepaSubmitPaymentExecutionContextAdapter;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.submit.SepaSubmitPaymentExecutionContextAdapterBuilder;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentAuthorizationUrlExtractor;
import com.yolt.providers.common.rest.http.HttpErrorHandlerV2;
import com.yolt.providers.stet.generic.GenericPaymentProviderV3;
import com.yolt.providers.stet.generic.auth.AuthenticationMeansSupplier;
import com.yolt.providers.stet.generic.config.DefaultProperties;
import com.yolt.providers.stet.generic.domain.ProviderIdentification;
import com.yolt.providers.stet.generic.domain.Region;
import com.yolt.providers.stet.generic.domain.Scope;
import com.yolt.providers.stet.generic.dto.payment.StetPaymentConfirmationRequestDTO;
import com.yolt.providers.stet.generic.dto.payment.StetPaymentInitiationRequestDTO;
import com.yolt.providers.stet.generic.dto.payment.response.StetPaymentInitiationResponseDTO;
import com.yolt.providers.stet.generic.dto.payment.response.StetPaymentStatusResponseDTO;
import com.yolt.providers.stet.generic.http.client.HttpClientFactory;
import com.yolt.providers.stet.generic.mapper.DateTimeSupplier;
import com.yolt.providers.stet.generic.mapper.providerstate.DefaultProviderStateMapper;
import com.yolt.providers.stet.generic.service.pec.authorization.token.*;
import com.yolt.providers.stet.generic.service.pec.common.StetPaymentHttpHeadersFactory;
import com.yolt.providers.stet.generic.service.pec.common.StetPaymentProviderStateExtractor;
import com.yolt.providers.stet.generic.service.pec.common.StetRawBankPaymentStatusMapper;
import com.yolt.providers.stet.generic.service.pec.confirmation.StetConfirmationPreExecutionResult;
import com.yolt.providers.stet.generic.service.pec.confirmation.status.*;
import com.yolt.providers.stet.generic.service.pec.confirmation.submit.*;
import com.yolt.providers.stet.generic.service.pec.initiate.StetInitiatePaymentHttpHeadersProvider;
import com.yolt.providers.stet.generic.service.pec.initiate.StetInitiatePaymentStatusesExtractor;
import com.yolt.providers.stet.generic.service.pec.initiate.StetInitiatePreExecutionResult;
import com.yolt.providers.stet.lclgroup.common.pec.authorization.token.LclGroupTokenPaymentHttpRequestBodyProvider;
import com.yolt.providers.stet.lclgroup.common.pec.confirmation.LclGroupSubmitPaymentAuthenticationFactorExtractor;
import com.yolt.providers.stet.lclgroup.common.pec.initiate.*;
import lombok.RequiredArgsConstructor;

import java.time.Clock;

@RequiredArgsConstructor
public class LclGroupGenericPaymentProviderV2Factory {

    private final ProviderIdentification providerIdentification;
    private final PaymentAuthorizationUrlExtractor<StetPaymentInitiationResponseDTO, StetInitiatePreExecutionResult> authorizationUrlExtractor;
    private final StetPaymentHttpHeadersFactory httpHeadersFactory;
    private final HttpClientFactory httpClientFactory;
    private final HttpErrorHandlerV2 httpErrorHandler;
    private final AuthenticationMeansSupplier authMeansSupplier;
    private final DateTimeSupplier dateTimeSupplier;
    private final DefaultProperties properties;
    private final ConsentValidityRules consentValidityRules;
    private final Scope paymentScope;
    private final ObjectMapper objectMapper;
    private final Clock clock;
    private final LclGroupPaymentHeadersExtractor headersExtractor;

    public GenericPaymentProviderV3 createPaymentProvider() {

        var tokenPaymentPreExecutionResultMapper = new StetTokenPaymentPreExecutionResultMapperV2();
        var tokenHttpRequestInvoker = new StetTokenPaymentHttpRequestInvokerV2(
                new LclGroupTokenPaymentHttpRequestBodyProvider(paymentScope),
                httpHeadersFactory,
                httpErrorHandler);

        return new GenericPaymentProviderV3(
                providerIdentification,
                createInitiatePECAdapter(tokenPaymentPreExecutionResultMapper, tokenHttpRequestInvoker),
                createSubmitPecAdapter(httpHeadersFactory, tokenPaymentPreExecutionResultMapper, tokenHttpRequestInvoker),
                createStatusPecAdapter(httpHeadersFactory, tokenPaymentPreExecutionResultMapper, tokenHttpRequestInvoker),
                authMeansSupplier,
                consentValidityRules);
    }

    private SepaInitiateSinglePaymentExecutionContextAdapter<StetPaymentInitiationRequestDTO, StetPaymentInitiationResponseDTO, StetInitiatePreExecutionResult> createInitiatePECAdapter(SepaTokenPaymentPreExecutionResultMapperV2<StetTokenPaymentPreExecutionResult> tokenPaymentPreExecutionResultMapper,
                                                                                                                                                                                         SepaTokenPaymentHttpRequestInvoker<StetTokenPaymentPreExecutionResult> tokenHttpRequestInvoker) {
        return SepaInitiateSinglePaymentExecutionContextAdapterBuilder.<StetPaymentInitiationRequestDTO, StetPaymentInitiationResponseDTO, StetInitiatePreExecutionResult>builder(
                        new StetInitiatePaymentStatusesExtractor(),
                        authorizationUrlExtractor,
                        new StetPaymentProviderStateExtractor<>(new LclGroupInitiatePaymentPaymentIdExtractor(headersExtractor), objectMapper),
                        objectMapper,
                        clock,
                        StetPaymentInitiationResponseDTO.class)
                .withPreExecutionResultMapper(new LclGroupInitiateSinglePaymentPreExecutionResultMapper(
                        authMeansSupplier,
                        providerIdentification,
                        tokenPaymentPreExecutionResultMapper,
                        tokenHttpRequestInvoker,
                        () -> "/pisp/1.0/payment-requests",
                        httpClientFactory,
                        Region::getBaseUrl,
                        properties))
                .withHttpRequestBodyProvider(new LclGroupInitiatePaymentHttpRequestBodyProvider(clock, dateTimeSupplier))
                .withHttpHeadersProvider(new StetInitiatePaymentHttpHeadersProvider(httpHeadersFactory))
                .withHttpRequestInvoker(new LclInitiatePaymentHttpRequestInvoker(httpErrorHandler, headersExtractor))
                .withResponseBodyValidator(new LclInitiatePaymentResponseValidator(new LclGroupInitiatePaymentPaymentIdExtractor(headersExtractor)))
                .build(new StetRawBankPaymentStatusMapper(objectMapper));
    }

    private SepaSubmitPaymentExecutionContextAdapter<StetPaymentConfirmationRequestDTO, StetPaymentStatusResponseDTO, StetConfirmationPreExecutionResult> createSubmitPecAdapter(StetPaymentHttpHeadersFactory httpHeadersFactory,
                                                                                                                                                                                 SepaTokenPaymentPreExecutionResultMapperV2 tokenPaymentPreExecutionResultMapper,
                                                                                                                                                                                 SepaTokenPaymentHttpRequestInvoker tokenPaymentHttpRequestInvoker) {

        StetSubmitPaymentPaymentIdExtractor paymentIdExtractor = new StetSubmitPaymentPaymentIdExtractor();
        StetPaymentProviderStateExtractor providerStateExtractor = new StetPaymentProviderStateExtractor<>(new StetSubmitPaymentPaymentIdExtractor(), objectMapper);
        return SepaSubmitPaymentExecutionContextAdapterBuilder.<StetPaymentConfirmationRequestDTO, StetPaymentStatusResponseDTO, StetConfirmationPreExecutionResult>builder(
                        new StetSubmitPaymentStatusesExtractor(),
                        paymentIdExtractor,
                        providerStateExtractor,
                        objectMapper,
                        clock,
                        StetPaymentStatusResponseDTO.class
                )
                .withPreExecutionResultMapper(new StetSubmitPaymentPreExecutionResultMapperV2<>(
                        authMeansSupplier,
                        providerStateExtractor,
                        providerIdentification,
                        tokenPaymentPreExecutionResultMapper,
                        tokenPaymentHttpRequestInvoker,
                        () -> "/pisp/1.0/payment-requests/{paymentRequestResourceId}/confirmation",
                        httpClientFactory,
                        Region::getBaseUrl,
                        properties
                ))
                .withHttpRequestBodyProvider(new StetSubmitPaymentHttpRequestBodyProvider(new LclGroupSubmitPaymentAuthenticationFactorExtractor()))
                .withHttpHeadersProvider(new StetSubmitPaymentHttpHeadersProvider(httpHeadersFactory))
                .withHttpRequestInvoker(new StetSubmitPaymentHttpRequestInvokerV2(httpErrorHandler))
                .withResponseBodyValidator(new StetSubmitResponseBodyValidator())
                .build(new StetRawBankPaymentStatusMapper(objectMapper));
    }

    private SepaStatusPaymentExecutionContextAdapter<StetPaymentConfirmationRequestDTO, StetPaymentStatusResponseDTO, StetConfirmationPreExecutionResult> createStatusPecAdapter(StetPaymentHttpHeadersFactory httpHeadersFactory,
                                                                                                                                                                                 SepaTokenPaymentPreExecutionResultMapperV2 tokenPaymentPreExecutionResultMapper,
                                                                                                                                                                                 SepaTokenPaymentHttpRequestInvoker tokenPaymentHttpRequestInvoker) {
        return SepaStatusPaymentExecutionContextAdapterBuilder.<StetPaymentConfirmationRequestDTO, StetPaymentStatusResponseDTO, StetConfirmationPreExecutionResult>builder(
                        new StetStatusPaymentStatusesExtractor(),
                        new StetStatusPaymentPaymentIdExtractor(),
                        new StetPaymentProviderStateExtractor<>(new StetSubmitPaymentPaymentIdExtractor(), objectMapper),
                        objectMapper,
                        clock,
                        StetPaymentStatusResponseDTO.class
                )
                .withPreExecutionResultMapper(new StetStatusPaymentPreExecutionResultMapperV2<>(
                        authMeansSupplier,
                        providerIdentification,
                        tokenPaymentPreExecutionResultMapper,
                        tokenPaymentHttpRequestInvoker,
                        new DefaultProviderStateMapper(objectMapper),
                        () -> "/pisp/1.0/payment-requests/{paymentRequestResourceId}",
                        httpClientFactory,
                        Region::getBaseUrl,
                        properties
                ))
                .withHttpHeadersProvider(new StetStatusPaymentHttpHeadersProvider(httpHeadersFactory))
                .withHttpRequestInvoker(new StetStatusPaymentHttpRequestInvokerV2(httpErrorHandler))
                .withResponseBodyValidator(new StetStatusResponseBodyValidator())
                .build(new StetRawBankPaymentStatusMapper(objectMapper));
    }
}
