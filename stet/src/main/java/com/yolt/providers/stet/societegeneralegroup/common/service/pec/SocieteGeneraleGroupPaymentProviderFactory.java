package com.yolt.providers.stet.societegeneralegroup.common.service.pec;

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
import com.yolt.providers.common.rest.tracing.ExternalTracingUtil;
import com.yolt.providers.stet.generic.GenericPaymentProviderV3;
import com.yolt.providers.stet.generic.auth.AuthenticationMeansSupplier;
import com.yolt.providers.stet.generic.config.DefaultProperties;
import com.yolt.providers.stet.generic.domain.DigestAlgorithm;
import com.yolt.providers.stet.generic.domain.ProviderIdentification;
import com.yolt.providers.stet.generic.domain.Region;
import com.yolt.providers.stet.generic.domain.Scope;
import com.yolt.providers.stet.generic.dto.payment.StetPaymentConfirmationRequestDTO;
import com.yolt.providers.stet.generic.dto.payment.StetPaymentInitiationRequestDTO;
import com.yolt.providers.stet.generic.dto.payment.response.StetPaymentInitiationResponseDTO;
import com.yolt.providers.stet.generic.dto.payment.response.StetPaymentStatusResponseDTO;
import com.yolt.providers.stet.generic.http.client.HttpClientFactory;
import com.yolt.providers.stet.generic.http.signer.DefaultHttpSigner;
import com.yolt.providers.stet.generic.mapper.providerstate.DefaultProviderStateMapper;
import com.yolt.providers.stet.generic.service.pec.authorization.StetPaymentAuthorizationUrlExtractor;
import com.yolt.providers.stet.generic.service.pec.authorization.token.*;
import com.yolt.providers.stet.generic.service.pec.common.StetPaymentHttpHeadersFactory;
import com.yolt.providers.stet.generic.service.pec.common.StetPaymentProviderStateExtractor;
import com.yolt.providers.stet.generic.service.pec.common.StetRawBankPaymentStatusMapper;
import com.yolt.providers.stet.generic.service.pec.confirmation.StetConfirmationPreExecutionResult;
import com.yolt.providers.stet.generic.service.pec.confirmation.status.*;
import com.yolt.providers.stet.generic.service.pec.confirmation.submit.*;
import com.yolt.providers.stet.generic.service.pec.initiate.*;
import com.yolt.providers.stet.societegeneralegroup.common.http.signer.signature.SocieteGeneraleGroupSignatureStrategy;
import com.yolt.providers.stet.societegeneralegroup.common.mapper.SocieteGeneraleDateTimeSupplier;
import com.yolt.providers.stet.societegeneralegroup.common.service.pec.common.SocieteGeneraleGroupSigningPaymentHttpHeadersFactory;
import com.yolt.providers.stet.societegeneralegroup.common.service.pec.initiate.SocieteGeneraleGroupInitiatePaymentIdExtractor;
import com.yolt.providers.stet.societegeneralegroup.common.service.pec.initiate.SocieteGeneraleGroupPaymentHttpRequestBodyProvider;
import com.yolt.providers.stet.societegeneralegroup.common.service.pec.initiate.SocieteGeneraleInitiatePaymentResponseBodyValidator;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;

import java.time.Clock;

@RequiredArgsConstructor
public class SocieteGeneraleGroupPaymentProviderFactory {

    private final ProviderIdentification providerIdentification;
    private final AuthenticationMeansSupplier authMeansSupplier;
    private final ConsentValidityRules consentValidityRules;
    private final ObjectMapper objectMapper;
    private final Clock clock;
    private final DefaultProperties properties;
    private final HttpClientFactory httpClientFactory;
    private final HttpErrorHandlerV2 errorHandler;


    public GenericPaymentProviderV3 createPaymentProvider() {

        DefaultHttpSigner httpSigner = new DefaultHttpSigner(new SocieteGeneraleGroupSignatureStrategy(SignatureAlgorithm.SHA256_WITH_RSA, properties),
                objectMapper,
                DigestAlgorithm.SHA_256);

        StetPaymentHttpHeadersFactory httpHeadersFactory = new SocieteGeneraleGroupSigningPaymentHttpHeadersFactory(httpSigner, ExternalTracingUtil::createLastExternalTraceId);

        SepaTokenPaymentPreExecutionResultMapperV2 tokenPaymentPreExecutionResultMapper = new StetTokenPaymentPreExecutionResultMapperV2();
        SepaTokenPaymentHttpRequestInvoker tokenPaymentHttpRequestInvoker = new StetTokenPaymentHttpRequestInvokerV2(new StetTokenPaymentHttpRequestBodyProvider(Scope.PISP),
                httpHeadersFactory,
                errorHandler);

        return new GenericPaymentProviderV3(providerIdentification,
                createInitiatePecAdapter(httpHeadersFactory, tokenPaymentPreExecutionResultMapper, tokenPaymentHttpRequestInvoker),
                createSubmitPecAdapter(httpHeadersFactory, tokenPaymentPreExecutionResultMapper, tokenPaymentHttpRequestInvoker),
                createStatusPecAdapter(httpHeadersFactory, tokenPaymentPreExecutionResultMapper, tokenPaymentHttpRequestInvoker),
                authMeansSupplier,
                consentValidityRules);
    }

    private SepaInitiateSinglePaymentExecutionContextAdapter<StetPaymentInitiationRequestDTO, StetPaymentInitiationResponseDTO, StetInitiatePreExecutionResult> createInitiatePecAdapter(StetPaymentHttpHeadersFactory httpHeadersFactory,
                                                                                                                                                                                         SepaTokenPaymentPreExecutionResultMapperV2 tokenPaymentPreExecutionResultMapper,
                                                                                                                                                                                         SepaTokenPaymentHttpRequestInvoker tokenPaymentHttpRequestInvoker) {
        PaymentAuthorizationUrlExtractor<StetPaymentInitiationResponseDTO, StetInitiatePreExecutionResult> authorizationUrlExtractor = new StetPaymentAuthorizationUrlExtractor();
        StetInitiatePaymentPaymentIdExtractor paymentIdExtractor = new SocieteGeneraleGroupInitiatePaymentIdExtractor(authorizationUrlExtractor);
        return SepaInitiateSinglePaymentExecutionContextAdapterBuilder.<StetPaymentInitiationRequestDTO, StetPaymentInitiationResponseDTO, StetInitiatePreExecutionResult>builder(
                new StetInitiatePaymentStatusesExtractor(),
                new StetPaymentAuthorizationUrlExtractor(),
                new StetPaymentProviderStateExtractor<>(paymentIdExtractor, objectMapper),
                objectMapper,
                clock,
                StetPaymentInitiationResponseDTO.class)
                .withPreExecutionResultMapper(new StetInitiateSinglePaymentPreExecutionResultMapperV3<>(
                        authMeansSupplier,
                        providerIdentification,
                        tokenPaymentPreExecutionResultMapper,
                        tokenPaymentHttpRequestInvoker,
                        httpClientFactory,
                        Region::getPisBaseUrl,
                        properties))
                .withHttpRequestBodyProvider(new SocieteGeneraleGroupPaymentHttpRequestBodyProvider(new SocieteGeneraleDateTimeSupplier(clock)))
                .withHttpHeadersProvider(new StetInitiatePaymentHttpHeadersProvider(httpHeadersFactory))
                .withHttpRequestInvoker(new StetInitiatePaymentHttpRequestInvokerV2(errorHandler))
                .withResponseBodyValidator(new SocieteGeneraleInitiatePaymentResponseBodyValidator(paymentIdExtractor))
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
                        httpClientFactory,
                        Region::getPisBaseUrl,
                        properties
                ))
                .withHttpHeadersProvider(new StetSubmitPaymentHttpHeadersProvider(httpHeadersFactory))
                .withHttpRequestInvoker(new StetSubmitPaymentHttpRequestInvokerV2(errorHandler))
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
                        httpClientFactory,
                        Region::getPisBaseUrl,
                        properties
                ))
                .withHttpHeadersProvider(new StetStatusPaymentHttpHeadersProvider(httpHeadersFactory))
                .withHttpRequestInvoker(new StetStatusPaymentHttpRequestInvokerV2(errorHandler))
                .withResponseBodyValidator(new StetStatusResponseBodyValidator())
                .build(new StetRawBankPaymentStatusMapper(objectMapper));
    }
}
