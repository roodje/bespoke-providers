package com.yolt.providers.openbanking.ais.tsbgroup.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
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
import com.yolt.providers.openbanking.ais.generic2.pec.DefaultPaymentExecutionContextAdapterFactory;
import com.yolt.providers.openbanking.ais.generic2.pec.auth.GenericPaymentAccessTokenProvider;
import com.yolt.providers.openbanking.ais.generic2.pec.auth.GenericPaymentAuthorizationCodeExtractor;
import com.yolt.providers.openbanking.ais.generic2.pec.auth.GenericPaymentRedirectUrlExtractor;
import com.yolt.providers.openbanking.ais.generic2.pec.common.*;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.datainitiation.PaymentDataInitiationMapper;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.status.GenericConsentResponseStatusMapper;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.status.GenericDelegatingPaymentStatusResponseMapper;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.status.GenericStatusResponseStatusMapper;
import com.yolt.providers.openbanking.ais.generic2.pec.restclient.DefaultPisRestClient;
import com.yolt.providers.openbanking.ais.generic2.pec.restclient.PisRestClient;
import com.yolt.providers.openbanking.ais.generic2.pec.status.model.PaymentStatusResponse;
import com.yolt.providers.openbanking.ais.generic2.pec.status.single.*;
import com.yolt.providers.openbanking.ais.generic2.pec.submit.single.*;
import com.yolt.providers.openbanking.ais.generic2.service.AuthenticationService;
import com.yolt.providers.openbanking.ais.generic2.signer.PaymentRequestSigner;
import com.yolt.providers.openbanking.ais.tsbgroup.common.mapper.TsbWriteDomesticResponseStatusMapper;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBRisk1;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomestic2;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticResponse5;

import java.time.Clock;
import java.util.Map;
import java.util.function.Function;

public class TsbPaymentExecutionContextAdapterFactory extends DefaultPaymentExecutionContextAdapterFactory {

    private final ProviderIdentification providerIdentification;
    private final ObjectMapper objectMapper;
    private final Function<Map<String, BasicAuthenticationMean>, DefaultAuthMeans> authMeansProvider;
    private final HttpClientFactory httpClientFactory;
    private final OBRisk1.PaymentContextCodeEnum paymentContextCode;
    private final EndpointsVersionable endpointsVersionable;
    private final Clock clock;
    private final UkProviderStateDeserializer ukProviderStateDeserializer;

    // internally computed properties
    private final GenericPaymentAccessTokenProvider paymentAccessTokenProvider;
    private final PaymentHttpHeadersFactory httpHeadersFactory;
    private final PisRestClient pisRestClient;
    private final RawBankPaymentStatusMapper rawBankPaymentStatusMapper;

    public TsbPaymentExecutionContextAdapterFactory(ProviderIdentification providerIdentification,
                                                    ObjectMapper objectMapper,
                                                    Function<Map<String, BasicAuthenticationMean>, DefaultAuthMeans> authMeansProvider,
                                                    AuthenticationService authenticationService,
                                                    HttpClientFactory httpClientFactory,
                                                    PaymentDataInitiationMapper ukDomesticDataInitiationMapper,
                                                    OBRisk1.PaymentContextCodeEnum paymentContextCode,
                                                    EndpointsVersionable endpointsVersionable,
                                                    PaymentRequestSigner paymentRequestSigner,
                                                    Clock clock,
                                                    final TokenScope tokenScope) {
        super(providerIdentification, objectMapper, authMeansProvider, authenticationService, httpClientFactory, ukDomesticDataInitiationMapper, paymentContextCode, endpointsVersionable, paymentRequestSigner, clock, tokenScope, new GenericConsentResponseStatusMapper(), new GenericStatusResponseStatusMapper());
        this.providerIdentification = providerIdentification;
        this.objectMapper = objectMapper;
        this.authMeansProvider = authMeansProvider;
        this.httpClientFactory = httpClientFactory;
        this.paymentContextCode = paymentContextCode;
        this.endpointsVersionable = endpointsVersionable;
        this.clock = clock;
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
    }

    @Override
    public UkSubmitPaymentExecutionContextAdapter<OBWriteDomestic2, OBWriteDomesticResponse5, GenericSubmitPaymentPreExecutionResult> createSubmitPaymentExecutionContextAdapter() {
        GenericSubmitPaymentStatusesExtractor paymentStatusesExtractor = new GenericSubmitPaymentStatusesExtractor(new TsbWriteDomesticResponseStatusMapper());
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

    @Override
    public UkStatusPaymentExecutionContextAdapter<Void, PaymentStatusResponse, GenericPaymentStatusPreExecutionResult> createStatusPaymentExecutionContextAdapter() {
        GenericPaymentStatusStatusesExtractor paymentStatusesExtractor = new GenericPaymentStatusStatusesExtractor(
                new GenericDelegatingPaymentStatusResponseMapper(new GenericConsentResponseStatusMapper(),
                        new TsbWriteDomesticResponseStatusMapper()));
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
