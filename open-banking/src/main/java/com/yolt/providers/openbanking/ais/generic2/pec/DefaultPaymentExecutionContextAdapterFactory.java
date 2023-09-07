package com.yolt.providers.openbanking.ais.generic2.pec;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.uk.initiate.UkInitiatePaymentExecutionContextAdapter;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.uk.initiate.UkInitiatePaymentExecutionContextAdapterBuilder;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.uk.status.UkStatusPaymentExecutionContextAdapter;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.uk.status.UkStatusPaymentExecutionContextAdapterBuilder;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.uk.submit.UkSubmitPaymentExecutionContextAdapter;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.uk.submit.UkSubmitPaymentExecutionContextAdapterBuilder;
import com.yolt.providers.common.pis.paymentexecutioncontext.errorhandler.RawBankPaymentStatusMapper;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.common.EndpointsVersionable;
import com.yolt.providers.openbanking.ais.generic2.common.ProviderIdentification;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClientFactory;
import com.yolt.providers.openbanking.ais.generic2.pec.auth.GenericPaymentAccessTokenProvider;
import com.yolt.providers.openbanking.ais.generic2.pec.auth.GenericPaymentAuthorizationCodeExtractor;
import com.yolt.providers.openbanking.ais.generic2.pec.auth.GenericPaymentRedirectUrlExtractor;
import com.yolt.providers.openbanking.ais.generic2.pec.common.*;
import com.yolt.providers.openbanking.ais.generic2.pec.initiate.single.*;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.datainitiation.PaymentDataInitiationMapper;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.status.GenericDelegatingPaymentStatusResponseMapper;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.status.ResponseStatusMapper;
import com.yolt.providers.openbanking.ais.generic2.pec.restclient.DefaultPisRestClient;
import com.yolt.providers.openbanking.ais.generic2.pec.restclient.PisRestClient;
import com.yolt.providers.openbanking.ais.generic2.pec.status.model.PaymentStatusResponse;
import com.yolt.providers.openbanking.ais.generic2.pec.status.single.*;
import com.yolt.providers.openbanking.ais.generic2.pec.submit.single.*;
import com.yolt.providers.openbanking.ais.generic2.service.AuthenticationService;
import com.yolt.providers.openbanking.ais.generic2.signer.PaymentRequestSigner;
import com.yolt.providers.openbanking.dto.pis.openbanking316.*;

import java.time.Clock;
import java.util.Map;
import java.util.function.Function;

public class DefaultPaymentExecutionContextAdapterFactory {

    private final ProviderIdentification providerIdentification;
    private final ObjectMapper objectMapper;
    private final Function<Map<String, BasicAuthenticationMean>, DefaultAuthMeans> authMeansProvider;
    private final AuthenticationService authenticationService;
    private final HttpClientFactory httpClientFactory;
    private final PaymentDataInitiationMapper ukDomesticDataInitiationMapper;
    private final OBRisk1.PaymentContextCodeEnum paymentContextCode;
    private final EndpointsVersionable endpointsVersionable;
    private final Clock clock;
    private final UkProviderStateDeserializer ukProviderStateDeserializer;
    private final TokenScope tokenScope;
    private final ResponseStatusMapper<OBWriteDomesticConsentResponse5Data.StatusEnum> consentResponseStatusMapper;
    private final ResponseStatusMapper<OBWriteDomesticResponse5Data.StatusEnum> responseStatusMapper;

    // internally computed properties
    private final GenericPaymentAccessTokenProvider paymentAccessTokenProvider;
    private final PaymentHttpHeadersFactory httpHeadersFactory;
    private final PisRestClient pisRestClient;
    private final RawBankPaymentStatusMapper rawBankPaymentStatusMapper;


    public DefaultPaymentExecutionContextAdapterFactory(ProviderIdentification providerIdentification,
                                                        ObjectMapper objectMapper,
                                                        Function<Map<String, BasicAuthenticationMean>, DefaultAuthMeans> authMeansProvider,
                                                        AuthenticationService authenticationService,
                                                        HttpClientFactory httpClientFactory,
                                                        PaymentDataInitiationMapper ukDomesticDataInitiationMapper,
                                                        OBRisk1.PaymentContextCodeEnum paymentContextCode,
                                                        EndpointsVersionable endpointsVersionable,
                                                        PaymentRequestSigner paymentRequestSigner,
                                                        Clock clock,
                                                        TokenScope tokenScope,
                                                        ResponseStatusMapper<OBWriteDomesticConsentResponse5Data.StatusEnum> consentResponseStatusMapper,
                                                        ResponseStatusMapper<OBWriteDomesticResponse5Data.StatusEnum> responseStatusMapper) {
        this.providerIdentification = providerIdentification;
        this.objectMapper = objectMapper;
        this.authMeansProvider = authMeansProvider;
        this.authenticationService = authenticationService;
        this.httpClientFactory = httpClientFactory;
        this.ukDomesticDataInitiationMapper = ukDomesticDataInitiationMapper;
        this.paymentContextCode = paymentContextCode;
        this.endpointsVersionable = endpointsVersionable;
        this.clock = clock;
        this.tokenScope = tokenScope;
        this.paymentAccessTokenProvider = new GenericPaymentAccessTokenProvider(httpClientFactory,
                authenticationService,
                new GenericPaymentAuthorizationCodeExtractor(),
                new GenericPaymentRedirectUrlExtractor(),
                providerIdentification,
                tokenScope);
        this.httpHeadersFactory = new GenericPaymentHttpHeadersFactory(paymentRequestSigner, new RandomUUIDPaymentRequestKeyProvider());
        this.pisRestClient = new DefaultPisRestClient(e -> {
            throw e;
        });
        this.rawBankPaymentStatusMapper = new GenericRawBankPaymentStatusMapper(objectMapper);
        this.ukProviderStateDeserializer = new UkProviderStateDeserializer(objectMapper);
        this.consentResponseStatusMapper = consentResponseStatusMapper;
        this.responseStatusMapper = responseStatusMapper;
    }


    public DefaultPaymentExecutionContextAdapterFactory(ProviderIdentification providerIdentification,
                                                        ObjectMapper objectMapper,
                                                        Function<Map<String, BasicAuthenticationMean>, DefaultAuthMeans> authMeansProvider,
                                                        AuthenticationService authenticationService,
                                                        HttpClientFactory httpClientFactory,
                                                        PaymentDataInitiationMapper ukDomesticDataInitiationMapper,
                                                        OBRisk1.PaymentContextCodeEnum paymentContextCode,
                                                        EndpointsVersionable endpointsVersionable,
                                                        PaymentHttpHeadersFactory paymentHttpHeadersFactory,
                                                        Clock clock,
                                                        TokenScope tokenScope,
                                                        ResponseStatusMapper<OBWriteDomesticConsentResponse5Data.StatusEnum> consentResponseStatusMapper,
                                                        ResponseStatusMapper<OBWriteDomesticResponse5Data.StatusEnum> responseStatusMapper) {
        this.providerIdentification = providerIdentification;
        this.objectMapper = objectMapper;
        this.authMeansProvider = authMeansProvider;
        this.authenticationService = authenticationService;
        this.httpClientFactory = httpClientFactory;
        this.ukDomesticDataInitiationMapper = ukDomesticDataInitiationMapper;
        this.paymentContextCode = paymentContextCode;
        this.endpointsVersionable = endpointsVersionable;
        this.clock = clock;
        this.tokenScope = tokenScope;
        this.paymentAccessTokenProvider = new GenericPaymentAccessTokenProvider(httpClientFactory,
                authenticationService,
                new GenericPaymentAuthorizationCodeExtractor(),
                new GenericPaymentRedirectUrlExtractor(),
                providerIdentification,
                tokenScope);
        this.httpHeadersFactory = paymentHttpHeadersFactory;
        this.pisRestClient = new DefaultPisRestClient(e -> {
            throw e;
        });
        this.rawBankPaymentStatusMapper = new GenericRawBankPaymentStatusMapper(objectMapper);
        this.ukProviderStateDeserializer = new UkProviderStateDeserializer(objectMapper);
        this.consentResponseStatusMapper = consentResponseStatusMapper;
        this.responseStatusMapper = responseStatusMapper;
    }

    public UkInitiatePaymentExecutionContextAdapter<OBWriteDomesticConsent4, OBWriteDomesticConsentResponse5, GenericInitiatePaymentPreExecutionResult>
    createInitiatePaymentExecutionContextAdapter() {

        GenericInitiatePaymentStatusesExtractor paymentStatusesExtractor = new GenericInitiatePaymentStatusesExtractor(consentResponseStatusMapper);
        GenericInitiatePaymentAuthorizationUrlExtractor authorizationUrlExtractor = new GenericInitiatePaymentAuthorizationUrlExtractor(this.tokenScope, authenticationService);
        GenericInitiatePaymentProviderStateExtractor genericUkInitiatePaymentProviderStateExtractor = new GenericInitiatePaymentProviderStateExtractor(objectMapper);
        GenericUkDomesticInitiatePaymentPreExecutionResultMapper ukDomesticInitiatePaymentPreExecutionResultMapper = new GenericUkDomesticInitiatePaymentPreExecutionResultMapper(authMeansProvider, paymentAccessTokenProvider);
        GenericInitiatePaymentHttpHeadersProvider httpHeadersProvider = new GenericInitiatePaymentHttpHeadersProvider(httpHeadersFactory);
        GenericInitiatePaymentHttpRequestBodyProvider httpRequestBodyProvider = new GenericInitiatePaymentHttpRequestBodyProvider(ukDomesticDataInitiationMapper, paymentContextCode);
        GenericInitiatePaymentHttpRequestInvoker httpRequestInvoker = new GenericInitiatePaymentHttpRequestInvoker(pisRestClient, httpClientFactory, endpointsVersionable, providerIdentification);
        GenericInitiatePaymentResponseBodyValidator responseBodyValidator = new GenericInitiatePaymentResponseBodyValidator();
        return UkInitiatePaymentExecutionContextAdapterBuilder.<OBWriteDomesticConsent4, OBWriteDomesticConsentResponse5, GenericInitiatePaymentPreExecutionResult>builder(paymentStatusesExtractor,
                        authorizationUrlExtractor,
                        genericUkInitiatePaymentProviderStateExtractor,
                        objectMapper,
                        clock,
                        OBWriteDomesticConsentResponse5.class)
                .withUkDomesticInitiatePaymentPreExecutionResultMapper(ukDomesticInitiatePaymentPreExecutionResultMapper)
                .withHttpHeadersProvider(httpHeadersProvider)
                .withHttpRequestBodyProvider(httpRequestBodyProvider)
                .withHttpRequestInvoker(httpRequestInvoker)
                .withResponseBodyValidator(responseBodyValidator)
                .build(rawBankPaymentStatusMapper);
    }

    public UkSubmitPaymentExecutionContextAdapter<OBWriteDomestic2, OBWriteDomesticResponse5, GenericSubmitPaymentPreExecutionResult> createSubmitPaymentExecutionContextAdapter() {
        GenericSubmitPaymentStatusesExtractor paymentStatusesExtractor = new GenericSubmitPaymentStatusesExtractor(responseStatusMapper);
        GenericSubmitPaymentIdExtractor paymentIdExtractor = new GenericSubmitPaymentIdExtractor();
        GenericSubmitPaymentProviderStateExtractor genericUkSubmitPaymentProviderStateExtractor = new GenericSubmitPaymentProviderStateExtractor(objectMapper);
        GenericSubmitPreExecutionResultMapper ukDomesticSubmitPreExecutionResultMapper = new GenericSubmitPreExecutionResultMapper(authMeansProvider, paymentAccessTokenProvider, ukProviderStateDeserializer);
        GenericSubmitPaymentHttpHeadersProvider httpHeadersProvider = new GenericSubmitPaymentHttpHeadersProvider(httpHeadersFactory);
        GenericSubmitPaymentHttpRequestBodyProvider httpRequestBodyProvider = new GenericSubmitPaymentHttpRequestBodyProvider(objectMapper, paymentContextCode);
        GenericSubmitPaymentHttpRequestInvoker httpRequestInvoker = new GenericSubmitPaymentHttpRequestInvoker(httpClientFactory, pisRestClient, endpointsVersionable, providerIdentification);
        GenericSubmitPaymentResponseBodyValidator responseBodyValidator = new GenericSubmitPaymentResponseBodyValidator();
        return UkSubmitPaymentExecutionContextAdapterBuilder.<OBWriteDomestic2, OBWriteDomesticResponse5, GenericSubmitPaymentPreExecutionResult>builder(paymentStatusesExtractor,
                        paymentIdExtractor,
                        genericUkSubmitPaymentProviderStateExtractor,
                        objectMapper,
                        clock,
                        OBWriteDomesticResponse5.class)
                .withUkDomesticSubmitPreExecutionResultMapper(ukDomesticSubmitPreExecutionResultMapper)
                .withHttpHeadersProvider(httpHeadersProvider)
                .withHttpRequestBodyProvider(httpRequestBodyProvider)
                .withHttpRequestInvoker(httpRequestInvoker)
                .withResponseBodyValidator(responseBodyValidator)
                .build(rawBankPaymentStatusMapper);
    }

    public UkStatusPaymentExecutionContextAdapter<Void, PaymentStatusResponse, GenericPaymentStatusPreExecutionResult> createStatusPaymentExecutionContextAdapter() {
        GenericPaymentStatusStatusesExtractor paymentStatusesExtractor = new GenericPaymentStatusStatusesExtractor(
                new GenericDelegatingPaymentStatusResponseMapper(consentResponseStatusMapper,
                        responseStatusMapper));
        GenericPaymentStatusProviderStateExtractor genericUkStatusPaymentProviderStateExtractor = new GenericPaymentStatusProviderStateExtractor(objectMapper);
        GenericPaymentStatusResourceIdExtractor resourceIdExtractor = new GenericPaymentStatusResourceIdExtractor();
        GenericPaymentStatusPreExecutionResultMapper preExecutionResultMapper = new GenericPaymentStatusPreExecutionResultMapper(authMeansProvider, paymentAccessTokenProvider, ukProviderStateDeserializer);
        GenericPaymentStatusHttpHeadersProvider httpHeadersProvider = new GenericPaymentStatusHttpHeadersProvider(httpHeadersFactory);
        GenericPaymentStatusHttpRequestInvoker httpRequestInvoker = new GenericPaymentStatusHttpRequestInvoker(httpClientFactory, pisRestClient, endpointsVersionable, providerIdentification);
        GenericPaymentStatusResponseBodyValidator responseBodyValidator = new GenericPaymentStatusResponseBodyValidator();
        return UkStatusPaymentExecutionContextAdapterBuilder.<Void, PaymentStatusResponse, GenericPaymentStatusPreExecutionResult>builder(paymentStatusesExtractor,
                        resourceIdExtractor,
                        genericUkStatusPaymentProviderStateExtractor,
                        objectMapper,
                        clock,
                        PaymentStatusResponse.class)
                .withUkDomesticStatusPaymentPreExecutionResultMapper(preExecutionResultMapper)
                .withHttpHeadersProvider(httpHeadersProvider)
                .withHttpRequestInvoker(httpRequestInvoker)
                .withResponseBodyValidator(responseBodyValidator)
                .build(rawBankPaymentStatusMapper);
    }
}
