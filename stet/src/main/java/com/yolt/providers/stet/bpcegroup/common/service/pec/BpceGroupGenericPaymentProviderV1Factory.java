package com.yolt.providers.stet.bpcegroup.common.service.pec;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.initiate.SepaInitiateSinglePaymentExecutionContextAdapter;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.initiate.SepaInitiateSinglePaymentExecutionContextAdapterBuilder;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.status.SepaStatusPaymentExecutionContextAdapter;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.status.SepaStatusPaymentExecutionContextAdapterBuilder;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.submit.SepaSubmitPaymentExecutionContextAdapter;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.submit.SepaSubmitPaymentExecutionContextAdapterBuilder;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentAuthorizationUrlExtractor;
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
import com.yolt.providers.stet.generic.http.error.NoActionHttpErrorHandlerV2;
import com.yolt.providers.stet.generic.mapper.DateTimeSupplier;
import com.yolt.providers.stet.generic.mapper.providerstate.ProviderStateMapper;
import com.yolt.providers.stet.generic.service.pec.authorization.token.*;
import com.yolt.providers.stet.generic.service.pec.common.StetPaymentHttpHeadersFactory;
import com.yolt.providers.stet.generic.service.pec.common.StetPaymentProviderStateExtractor;
import com.yolt.providers.stet.generic.service.pec.common.StetRawBankPaymentStatusMapper;
import com.yolt.providers.stet.generic.service.pec.confirmation.StetConfirmationPreExecutionResult;
import com.yolt.providers.stet.generic.service.pec.confirmation.status.*;
import com.yolt.providers.stet.generic.service.pec.confirmation.submit.*;
import com.yolt.providers.stet.generic.service.pec.initiate.*;
import lombok.RequiredArgsConstructor;

import java.time.Clock;

@RequiredArgsConstructor
public class BpceGroupGenericPaymentProviderV1Factory {

    private final ProviderIdentification providerIdentification;
    private final PaymentAuthorizationUrlExtractor<StetPaymentInitiationResponseDTO, StetInitiatePreExecutionResult> authorizationUrlExtractor;
    private final StetPaymentHttpHeadersFactory httpHeadersFactory;
    private final HttpClientFactory httpClientFactory;
    private final NoActionHttpErrorHandlerV2 httpErrorHandler;
    private final AuthenticationMeansSupplier authMeansSupplier;
    private final DateTimeSupplier dateTimeSupplier;
    private final ProviderStateMapper providerStateMapper;
    private final DefaultProperties properties;
    private final ConsentValidityRules consentValidityRules;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public GenericPaymentProviderV3 createPaymentProvider() {
        StetRawBankPaymentStatusMapper rawBankPaymentStatusMapper = new StetRawBankPaymentStatusMapper(objectMapper);
        StetTokenPaymentPreExecutionResultMapperV2 tokenPaymentPreExecutionResultMapper = new StetTokenPaymentPreExecutionResultMapperV2();
        StetTokenPaymentHttpRequestInvokerV2 tokenHttpRequestInvoker = new StetTokenPaymentHttpRequestInvokerV2(
                new BpceTokenPaymentHttpRequestBodyProvider(Scope.PISP),
                httpHeadersFactory,
                httpErrorHandler);

        StetStatusPaymentPreExecutionResultMapperV2<StetTokenPaymentPreExecutionResult> statusPaymentPreExecutionResultMapper = new StetStatusPaymentPreExecutionResultMapperV2<>(
                authMeansSupplier,
                providerIdentification,
                tokenPaymentPreExecutionResultMapper,
                tokenHttpRequestInvoker,
                providerStateMapper,
                () -> "/stet/psd2/v1.4.2/payment-requests/{paymentRequestResourceId}",
                httpClientFactory,
                Region::getBaseUrl,
                properties);


        return new GenericPaymentProviderV3(
                providerIdentification,
                createInitiatePECAdapter(httpHeadersFactory, tokenPaymentPreExecutionResultMapper, tokenHttpRequestInvoker),
                createSubmitPECAdapter(httpHeadersFactory, tokenPaymentPreExecutionResultMapper, tokenHttpRequestInvoker),
                createStatusPECAdapter(httpHeadersFactory, statusPaymentPreExecutionResultMapper, rawBankPaymentStatusMapper),
                authMeansSupplier,
                consentValidityRules);
    }

    private SepaInitiateSinglePaymentExecutionContextAdapter<StetPaymentInitiationRequestDTO, StetPaymentInitiationResponseDTO, StetInitiatePreExecutionResult> createInitiatePECAdapter(StetPaymentHttpHeadersFactory httpHeadersFactory, SepaTokenPaymentPreExecutionResultMapperV2 tokenPaymentPreExecutionResultMapper,
                                                                                                                                                                                         SepaTokenPaymentHttpRequestInvoker tokenPaymentHttpRequestInvoker) {
        return SepaInitiateSinglePaymentExecutionContextAdapterBuilder.<StetPaymentInitiationRequestDTO, StetPaymentInitiationResponseDTO, StetInitiatePreExecutionResult>builder(
                        new StetInitiatePaymentStatusesExtractor(),
                        authorizationUrlExtractor,
                        new StetPaymentProviderStateExtractor<>(new StetInitiatePaymentPaymentIdExtractor(() -> "paymentRequestRessourceId"), objectMapper),
                        objectMapper,
                        clock,
                        StetPaymentInitiationResponseDTO.class)
                .withPreExecutionResultMapper(new StetInitiateSinglePaymentPreExecutionResultMapperV3<>(
                        authMeansSupplier,
                        providerIdentification,
                        tokenPaymentPreExecutionResultMapper,
                        tokenPaymentHttpRequestInvoker,
                        () -> "/stet/psd2/v1.4.2/payment-requests",
                        httpClientFactory,
                        Region::getBaseUrl,
                        properties))
                .withHttpRequestBodyProvider(new BpceGroupInitiatePaymentHttpRequestBodyProvider(clock, dateTimeSupplier))
                .withHttpHeadersProvider(new StetInitiatePaymentHttpHeadersProvider(httpHeadersFactory))
                .withHttpRequestInvoker(new StetInitiatePaymentHttpRequestInvokerV2(httpErrorHandler))
                .build(new StetRawBankPaymentStatusMapper(objectMapper));
    }

    private SepaSubmitPaymentExecutionContextAdapter<StetPaymentConfirmationRequestDTO, StetPaymentStatusResponseDTO, StetConfirmationPreExecutionResult> createSubmitPECAdapter(StetPaymentHttpHeadersFactory httpHeadersFactory,
                                                                                                                                                                                 SepaTokenPaymentPreExecutionResultMapperV2<StetTokenPaymentPreExecutionResult> tokenPaymentPreExecutionResultMapper,
                                                                                                                                                                                 SepaTokenPaymentHttpRequestInvoker<StetTokenPaymentPreExecutionResult> tokenHttpRequestInvoker) {
        StetPaymentProviderStateExtractor<StetPaymentStatusResponseDTO, StetConfirmationPreExecutionResult> providerStateExtractor = new StetPaymentProviderStateExtractor<>(new StetSubmitPaymentPaymentIdExtractor(), objectMapper);
        StetSubmitPaymentPaymentIdExtractor statusPaymentIdExtractor = new StetSubmitPaymentPaymentIdExtractor();
        return SepaSubmitPaymentExecutionContextAdapterBuilder.<StetPaymentConfirmationRequestDTO, StetPaymentStatusResponseDTO, StetConfirmationPreExecutionResult>builder(
                        new StetSubmitPaymentStatusesExtractor(),
                        statusPaymentIdExtractor,
                        new StetPaymentProviderStateExtractor<>(statusPaymentIdExtractor, objectMapper),
                        objectMapper,
                        clock,
                        StetPaymentStatusResponseDTO.class)
                .withPreExecutionResultMapper(
                        new BpceSubmitPaymentPreExecutionResultMapper<>(
                                authMeansSupplier,
                                providerStateExtractor,
                                providerIdentification,
                                tokenPaymentPreExecutionResultMapper,
                                tokenHttpRequestInvoker,
                                () -> "/stet/psd2/v1.4.2/payment-requests/{paymentRequestResourceId}",
                                httpClientFactory,
                                Region::getBaseUrl,
                                properties
                        ))
                .withHttpHeadersProvider(new StetSubmitPaymentHttpHeadersProvider(httpHeadersFactory))
                .withHttpRequestInvoker(new StetSubmitPaymentHttpRequestInvokerV2(httpErrorHandler))
                .withResponseBodyValidator(new StetSubmitResponseBodyValidator())
                .withGetStatusAsSubmitStep()
                .build(new StetRawBankPaymentStatusMapper(objectMapper));
    }

    private SepaStatusPaymentExecutionContextAdapter<StetPaymentConfirmationRequestDTO, StetPaymentStatusResponseDTO, StetConfirmationPreExecutionResult> createStatusPECAdapter(StetPaymentHttpHeadersFactory httpHeadersFactory,
                                                                                                                                                                                 StetStatusPaymentPreExecutionResultMapperV2<StetTokenPaymentPreExecutionResult> statusPaymentPreExecutionResultMapper,
                                                                                                                                                                                 StetRawBankPaymentStatusMapper rawBankPaymentStatusMapper) {
        StetStatusPaymentPaymentIdExtractor statusPaymentIdExtractor = new StetStatusPaymentPaymentIdExtractor();
        return SepaStatusPaymentExecutionContextAdapterBuilder.<StetPaymentConfirmationRequestDTO, StetPaymentStatusResponseDTO, StetConfirmationPreExecutionResult>builder(
                        new StetStatusPaymentStatusesExtractor(),
                        statusPaymentIdExtractor,
                        new StetPaymentProviderStateExtractor<>(statusPaymentIdExtractor, objectMapper),
                        objectMapper,
                        clock,
                        StetPaymentStatusResponseDTO.class)
                .withPreExecutionResultMapper(statusPaymentPreExecutionResultMapper)
                .withHttpHeadersProvider(new StetStatusPaymentHttpHeadersProvider(httpHeadersFactory))
                .withHttpRequestInvoker(new StetStatusPaymentHttpRequestInvokerV2(httpErrorHandler))
                .withResponseBodyValidator(new StetStatusResponseBodyValidator())
                .build(rawBankPaymentStatusMapper);
    }
}
