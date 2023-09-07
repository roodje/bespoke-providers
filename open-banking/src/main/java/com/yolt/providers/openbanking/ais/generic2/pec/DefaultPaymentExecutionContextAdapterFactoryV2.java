package com.yolt.providers.openbanking.ais.generic2.pec;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.uk.initiate.UkInitiateScheduledPaymentExecutionContextAdapter;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.uk.initiate.UkInitiateScheduledPaymentExecutionContextAdapterBuilder;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.uk.initiate.UkInitiateSinglePaymentExecutionContextAdapter;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.uk.initiate.UkInitiateSinglePaymentExecutionContextAdapterBuilder;
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
import com.yolt.providers.openbanking.ais.generic2.pec.initiate.scheduled.*;
import com.yolt.providers.openbanking.ais.generic2.pec.initiate.single.*;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.datainitiation.PaymentDataInitiationMapper;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.datainitiation.ScheduledPaymentDataInitiationMapper;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.status.GenericDelegatingPaymentStatusResponseMapper;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.status.GenericDelegatingScheduledPaymentStatusResponseMapper;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.status.ResponseStatusMapper;
import com.yolt.providers.openbanking.ais.generic2.pec.restclient.DefaultPisRestClient;
import com.yolt.providers.openbanking.ais.generic2.pec.restclient.PisRestClient;
import com.yolt.providers.openbanking.ais.generic2.pec.status.model.PaymentStatusResponse;
import com.yolt.providers.openbanking.ais.generic2.pec.status.model.ScheduledPaymentStatusResponse;
import com.yolt.providers.openbanking.ais.generic2.pec.status.scheduled.*;
import com.yolt.providers.openbanking.ais.generic2.pec.status.single.*;
import com.yolt.providers.openbanking.ais.generic2.pec.submit.scheduled.*;
import com.yolt.providers.openbanking.ais.generic2.pec.submit.single.*;
import com.yolt.providers.openbanking.ais.generic2.service.AuthenticationService;
import com.yolt.providers.openbanking.ais.generic2.signer.PaymentRequestSigner;
import com.yolt.providers.openbanking.dto.pis.openbanking316.*;

import java.time.Clock;
import java.util.Map;
import java.util.function.Function;

public class DefaultPaymentExecutionContextAdapterFactoryV2 {

    private final ProviderIdentification providerIdentification;
    private final ObjectMapper objectMapper;
    private final Function<Map<String, BasicAuthenticationMean>, DefaultAuthMeans> authMeansProvider;
    private final AuthenticationService authenticationService;
    private final HttpClientFactory httpClientFactory;
    private final PaymentDataInitiationMapper ukDomesticDataInitiationMapper;
    private final ScheduledPaymentDataInitiationMapper ukDomesticScheduledDataInitiationMapper;
    private final OBRisk1.PaymentContextCodeEnum paymentContextCode;
    private final EndpointsVersionable endpointsVersionable;
    private final Clock clock;
    private final UkProviderStateDeserializer ukProviderStateDeserializer;
    private final TokenScope tokenScope;
    private final ResponseStatusMapper<OBWriteDomesticConsentResponse5Data.StatusEnum> consentResponseStatusMapper;
    private final ResponseStatusMapper<OBWriteDomesticResponse5Data.StatusEnum> responseStatusMapper;
    private final ResponseStatusMapper<OBWriteDomesticScheduledConsentResponse5Data.StatusEnum> consentScheduledResponseStatusMapper;
    private final ResponseStatusMapper<OBWriteDomesticScheduledResponse5Data.StatusEnum> scheduledResponseStatusMapper;

    // internally computed properties
    private final GenericPaymentAccessTokenProvider paymentAccessTokenProvider;
    private final PaymentHttpHeadersFactory httpHeadersFactory;
    private final PisRestClient pisRestClient;
    private final RawBankPaymentStatusMapper rawBankPaymentStatusMapper;


    public DefaultPaymentExecutionContextAdapterFactoryV2(ProviderIdentification providerIdentification,
                                                          ObjectMapper objectMapper,
                                                          Function<Map<String, BasicAuthenticationMean>, DefaultAuthMeans> authMeansProvider,
                                                          AuthenticationService authenticationService,
                                                          HttpClientFactory httpClientFactory,
                                                          PaymentDataInitiationMapper ukDomesticDataInitiationMapper,
                                                          ScheduledPaymentDataInitiationMapper ukDomesticScheduledDataInitiationMapper,
                                                          OBRisk1.PaymentContextCodeEnum paymentContextCode,
                                                          EndpointsVersionable endpointsVersionable,
                                                          PaymentRequestSigner paymentRequestSigner,
                                                          Clock clock,
                                                          TokenScope tokenScope,
                                                          ResponseStatusMapper<OBWriteDomesticConsentResponse5Data.StatusEnum> consentResponseStatusMapper,
                                                          ResponseStatusMapper<OBWriteDomesticResponse5Data.StatusEnum> responseStatusMapper,
                                                          ResponseStatusMapper<OBWriteDomesticScheduledConsentResponse5Data.StatusEnum> consentScheduledResponseStatusMapper,
                                                          ResponseStatusMapper<OBWriteDomesticScheduledResponse5Data.StatusEnum> scheduledResponseStatusMapper) {
        this.providerIdentification = providerIdentification;
        this.objectMapper = objectMapper;
        this.authMeansProvider = authMeansProvider;
        this.authenticationService = authenticationService;
        this.httpClientFactory = httpClientFactory;
        this.ukDomesticDataInitiationMapper = ukDomesticDataInitiationMapper;
        this.ukDomesticScheduledDataInitiationMapper = ukDomesticScheduledDataInitiationMapper;
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
        this.consentScheduledResponseStatusMapper = consentScheduledResponseStatusMapper;
        this.scheduledResponseStatusMapper = scheduledResponseStatusMapper;
    }

    public UkInitiateSinglePaymentExecutionContextAdapter<OBWriteDomesticConsent4, OBWriteDomesticConsentResponse5, GenericInitiatePaymentPreExecutionResult>
    createInitiatePaymentExecutionContextAdapter() {

        GenericInitiatePaymentStatusesExtractor paymentStatusesExtractor = new GenericInitiatePaymentStatusesExtractor(consentResponseStatusMapper);
        GenericInitiatePaymentAuthorizationUrlExtractor authorizationUrlExtractor = new GenericInitiatePaymentAuthorizationUrlExtractor(this.tokenScope, authenticationService);
        GenericInitiatePaymentProviderStateExtractor genericUkInitiatePaymentProviderStateExtractor = new GenericInitiatePaymentProviderStateExtractor(objectMapper);
        GenericInitiatePaymentPreExecutionResultMapper ukDomesticInitiateSinglePaymentPreExecutionResultMapper = new GenericInitiatePaymentPreExecutionResultMapper(authMeansProvider, paymentAccessTokenProvider);
        GenericInitiatePaymentHttpHeadersProvider httpHeadersProvider = new GenericInitiatePaymentHttpHeadersProvider(httpHeadersFactory);
        GenericInitiatePaymentHttpRequestBodyProvider httpRequestBodyProvider = new GenericInitiatePaymentHttpRequestBodyProvider(ukDomesticDataInitiationMapper, paymentContextCode);
        GenericInitiatePaymentHttpRequestInvoker httpRequestInvoker = new GenericInitiatePaymentHttpRequestInvoker(pisRestClient, httpClientFactory, endpointsVersionable, providerIdentification);
        GenericInitiatePaymentResponseBodyValidator responseBodyValidator = new GenericInitiatePaymentResponseBodyValidator();
        return UkInitiateSinglePaymentExecutionContextAdapterBuilder.<OBWriteDomesticConsent4, OBWriteDomesticConsentResponse5, GenericInitiatePaymentPreExecutionResult>builder(paymentStatusesExtractor,
                        authorizationUrlExtractor,
                        genericUkInitiatePaymentProviderStateExtractor,
                        objectMapper,
                        clock,
                        OBWriteDomesticConsentResponse5.class)
                .withUkDomesticInitiatePaymentPreExecutionResultMapper(ukDomesticInitiateSinglePaymentPreExecutionResultMapper)
                .withHttpHeadersProvider(httpHeadersProvider)
                .withHttpRequestBodyProvider(httpRequestBodyProvider)
                .withHttpRequestInvoker(httpRequestInvoker)
                .withResponseBodyValidator(responseBodyValidator)
                .build(rawBankPaymentStatusMapper);
    }

    public UkInitiateScheduledPaymentExecutionContextAdapter<OBWriteDomesticScheduledConsent4, OBWriteDomesticScheduledConsentResponse5, GenericInitiateScheduledPaymentPreExecutionResult>
    createInitiateScheduledPaymentExecutionContextAdapter() {

        GenericInitiateScheduledPaymentStatusesExtractor paymentStatusesExtractor = new GenericInitiateScheduledPaymentStatusesExtractor(consentScheduledResponseStatusMapper);
        GenericInitiateScheduledPaymentAuthorizationUrlExtractor authorizationUrlExtractor = new GenericInitiateScheduledPaymentAuthorizationUrlExtractor(this.tokenScope, authenticationService);
        GenericInitiateScheduledPaymentProviderStateExtractor genericUkInitiateScheduledPaymentProviderStateExtractor = new GenericInitiateScheduledPaymentProviderStateExtractor(objectMapper);
        GenericInitiateScheduledPaymentPreExecutionResultMapper ukDomesticInitiateScheduledPaymentPreExecutionResultMapper = new GenericInitiateScheduledPaymentPreExecutionResultMapper(authMeansProvider, paymentAccessTokenProvider);
        GenericInitiateScheduledPaymentHttpHeadersProvider httpHeadersProvider = new GenericInitiateScheduledPaymentHttpHeadersProvider(httpHeadersFactory);
        GenericInitiateScheduledPaymentHttpRequestBodyProvider httpRequestBodyProvider = new GenericInitiateScheduledPaymentHttpRequestBodyProvider(ukDomesticScheduledDataInitiationMapper, paymentContextCode);
        GenericInitiateScheduledPaymentHttpRequestInvoker httpRequestInvoker = new GenericInitiateScheduledPaymentHttpRequestInvoker(pisRestClient, httpClientFactory, endpointsVersionable, providerIdentification);
        GenericInitiateScheduledPaymentResponseBodyValidator responseBodyValidator = new GenericInitiateScheduledPaymentResponseBodyValidator();
        return UkInitiateScheduledPaymentExecutionContextAdapterBuilder.<OBWriteDomesticScheduledConsent4, OBWriteDomesticScheduledConsentResponse5, GenericInitiateScheduledPaymentPreExecutionResult>builder(
                        paymentStatusesExtractor,
                        authorizationUrlExtractor,
                        genericUkInitiateScheduledPaymentProviderStateExtractor,
                        objectMapper,
                        clock,
                        OBWriteDomesticScheduledConsentResponse5.class)
                .withPreExecutionResultMapper(ukDomesticInitiateScheduledPaymentPreExecutionResultMapper)
                .withHttpHeadersProvider(httpHeadersProvider)
                .withHttpRequestBodyProvider(httpRequestBodyProvider)
                .withHttpRequestInvoker(httpRequestInvoker)
                .withResponseBodyValidator(responseBodyValidator)
                .build(rawBankPaymentStatusMapper);
    }

    public UkSubmitPaymentExecutionContextAdapter<OBWriteDomestic2, OBWriteDomesticResponse5, GenericSubmitPaymentPreExecutionResult> createSubmitPaymentExecutionContextAdapter
            () {
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

    public UkSubmitPaymentExecutionContextAdapter<OBWriteDomesticScheduled2, OBWriteDomesticScheduledResponse5, GenericSubmitPaymentPreExecutionResult> createSubmitScheduledPaymentExecutionContextAdapter
            () {
        GenericSubmitScheduledPaymentStatusesExtractor paymentStatusesExtractor = new GenericSubmitScheduledPaymentStatusesExtractor(scheduledResponseStatusMapper);
        GenericSubmitScheduledPaymentIdExtractor paymentIdExtractor = new GenericSubmitScheduledPaymentIdExtractor();
        GenericSubmitScheduledPaymentProviderStateExtractor genericUkSubmitPaymentProviderStateExtractor = new GenericSubmitScheduledPaymentProviderStateExtractor(objectMapper);
        GenericSubmitPreExecutionResultMapper ukDomesticSubmitPreExecutionResultMapper = new GenericSubmitPreExecutionResultMapper(authMeansProvider, paymentAccessTokenProvider, ukProviderStateDeserializer);
        GenericSubmitScheduledPaymentHttpHeadersProvider httpHeadersProvider = new GenericSubmitScheduledPaymentHttpHeadersProvider(httpHeadersFactory);
        GenericSubmitScheduledPaymentHttpRequestBodyProvider httpRequestBodyProvider = new GenericSubmitScheduledPaymentHttpRequestBodyProvider(objectMapper, paymentContextCode);
        GenericSubmitScheduledPaymentHttpRequestInvoker httpRequestInvoker = new GenericSubmitScheduledPaymentHttpRequestInvoker(httpClientFactory, pisRestClient, endpointsVersionable, providerIdentification);
        GenericSubmitScheduledPaymentResponseBodyValidator responseBodyValidator = new GenericSubmitScheduledPaymentResponseBodyValidator();
        return UkSubmitPaymentExecutionContextAdapterBuilder.<OBWriteDomesticScheduled2, OBWriteDomesticScheduledResponse5, GenericSubmitPaymentPreExecutionResult>builder(paymentStatusesExtractor,
                        paymentIdExtractor,
                        genericUkSubmitPaymentProviderStateExtractor,
                        objectMapper,
                        clock,
                        OBWriteDomesticScheduledResponse5.class)
                .withUkDomesticSubmitPreExecutionResultMapper(ukDomesticSubmitPreExecutionResultMapper)
                .withHttpHeadersProvider(httpHeadersProvider)
                .withHttpRequestBodyProvider(httpRequestBodyProvider)
                .withHttpRequestInvoker(httpRequestInvoker)
                .withResponseBodyValidator(responseBodyValidator)
                .build(rawBankPaymentStatusMapper);
    }

    public UkStatusPaymentExecutionContextAdapter<Void, PaymentStatusResponse, GenericPaymentStatusPreExecutionResult> createStatusPaymentExecutionContextAdapter
            () {
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

    public UkStatusPaymentExecutionContextAdapter<Void, ScheduledPaymentStatusResponse, GenericPaymentStatusPreExecutionResult> createStatusScheduledPaymentExecutionContextAdapter
            () {
        GenericScheduledPaymentStatusStatusesExtractor paymentStatusesExtractor = new GenericScheduledPaymentStatusStatusesExtractor(
                new GenericDelegatingScheduledPaymentStatusResponseMapper(consentScheduledResponseStatusMapper,
                        scheduledResponseStatusMapper));
        GenericScheduledPaymentStatusProviderStateExtractor genericUkStatusPaymentProviderStateExtractor = new GenericScheduledPaymentStatusProviderStateExtractor(objectMapper);
        GenericScheduledPaymentStatusResourceIdExtractor resourceIdExtractor = new GenericScheduledPaymentStatusResourceIdExtractor();
        GenericPaymentStatusPreExecutionResultMapper preExecutionResultMapper = new GenericPaymentStatusPreExecutionResultMapper(authMeansProvider, paymentAccessTokenProvider, ukProviderStateDeserializer);
        GenericPaymentStatusHttpHeadersProvider httpHeadersProvider = new GenericPaymentStatusHttpHeadersProvider(httpHeadersFactory);
        GenericScheduledPaymentStatusHttpRequestInvoker httpRequestInvoker = new GenericScheduledPaymentStatusHttpRequestInvoker(httpClientFactory, pisRestClient, endpointsVersionable, providerIdentification);
        GenericScheduledPaymentStatusResponseBodyValidator responseBodyValidator = new GenericScheduledPaymentStatusResponseBodyValidator();
        return UkStatusPaymentExecutionContextAdapterBuilder.<Void, ScheduledPaymentStatusResponse, GenericPaymentStatusPreExecutionResult>builder(paymentStatusesExtractor,
                        resourceIdExtractor,
                        genericUkStatusPaymentProviderStateExtractor,
                        objectMapper,
                        clock,
                        ScheduledPaymentStatusResponse.class)
                .withUkDomesticStatusPaymentPreExecutionResultMapper(preExecutionResultMapper)
                .withHttpHeadersProvider(httpHeadersProvider)
                .withHttpRequestInvoker(httpRequestInvoker)
                .withResponseBodyValidator(responseBodyValidator)
                .build(rawBankPaymentStatusMapper);
    }
}
