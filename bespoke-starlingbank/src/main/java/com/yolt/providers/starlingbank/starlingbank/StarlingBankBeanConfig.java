package com.yolt.providers.starlingbank.starlingbank;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.uk.initiate.UkInitiatePaymentExecutionContextAdapter;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.uk.initiate.UkInitiatePaymentExecutionContextAdapterBuilder;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.uk.status.UkStatusPaymentExecutionContextAdapter;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.uk.status.UkStatusPaymentExecutionContextAdapterBuilder;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.uk.submit.UkSubmitPaymentExecutionContextAdapter;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.uk.submit.UkSubmitPaymentExecutionContextAdapterBuilder;
import com.yolt.providers.common.pis.paymentexecutioncontext.errorhandler.DefaultPaymentExecutionContextPreExecutionErrorHandler;
import com.yolt.providers.common.pis.paymentexecutioncontext.exception.PaymentExecutionTechnicalException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.starlingbank.common.StarlingBankDataProvider;
import com.yolt.providers.starlingbank.common.StarlingBankPaymentProviderV2;
import com.yolt.providers.starlingbank.common.configuration.StarlingBankProperties;
import com.yolt.providers.starlingbank.common.errorhandler.StarlingPreExecutionErrorHandler;
import com.yolt.providers.starlingbank.common.http.StarlingBankHttpClientFactoryV4;
import com.yolt.providers.starlingbank.common.http.StarlingBankHttpErrorHandler;
import com.yolt.providers.starlingbank.common.http.StarlingBankPaymentHttpErrorHandler;
import com.yolt.providers.starlingbank.common.http.authorizationurlparametersproducer.StarlingBankAisAuthorizationUrlParametersProducer;
import com.yolt.providers.starlingbank.common.http.authorizationurlparametersproducer.StarlingBankPisAuthorizationUrlParametersProducer;
import com.yolt.providers.starlingbank.common.mapper.StarlingBankTokenMapper;
import com.yolt.providers.starlingbank.common.model.PaymentRequest;
import com.yolt.providers.starlingbank.common.model.PaymentStatusResponse;
import com.yolt.providers.starlingbank.common.model.PaymentSubmissionResponse;
import com.yolt.providers.starlingbank.common.paymentexecutioncontext.StarlingBankHttpHeadersProducerFactory;
import com.yolt.providers.starlingbank.common.paymentexecutioncontext.StarlingBankInitiatePaymentHttpRequestInvoker;
import com.yolt.providers.starlingbank.common.paymentexecutioncontext.StarlingBankStatusPaymentHttpRequestInvoker;
import com.yolt.providers.starlingbank.common.paymentexecutioncontext.StarlingBankSubmitPaymentHttpRequestInvoker;
import com.yolt.providers.starlingbank.common.paymentexecutioncontext.extractor.*;
import com.yolt.providers.starlingbank.common.paymentexecutioncontext.mapper.*;
import com.yolt.providers.starlingbank.common.paymentexecutioncontext.model.StarlingBankInitiatePaymentExecutionContextPreExecutionResult;
import com.yolt.providers.starlingbank.common.paymentexecutioncontext.model.StarlingBankSubmitAndStatusPaymentExecutionContextPreExecutionResult;
import com.yolt.providers.starlingbank.common.paymentexecutioncontext.provider.StarlingBankStatusPaymentHttpHeadersProvider;
import com.yolt.providers.starlingbank.common.paymentexecutioncontext.provider.StarlingBankSubmitPaymentHttpHeadersProvider;
import com.yolt.providers.starlingbank.common.paymentexecutioncontext.provider.StarlingBankSubmitPaymentHttpRequestBodyProvider;
import com.yolt.providers.starlingbank.common.paymentexecutioncontext.validator.StarlingBankStatusPaymentExecutionContextResponseBodyValidator;
import com.yolt.providers.starlingbank.common.paymentexecutioncontext.validator.StarlingBankSubmitPaymentExecutionContextResponseBodyValidator;
import com.yolt.providers.starlingbank.common.service.authorization.StarlingBankAuthorizationServiceV5;
import com.yolt.providers.starlingbank.common.service.fetchdata.StarlingBankFetchDataService;
import com.yolt.providers.starlingbank.common.service.fetchdata.StarlingBankFetchDataServiceV6;
import com.yolt.providers.starlingbank.starlingbank.auth.StarlingBankAisAuthMeansSupplierV2;
import com.yolt.providers.starlingbank.starlingbank.auth.StarlingBankPisAuthMeansSupplierV2;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Configuration
public class StarlingBankBeanConfig {

    private static final String PROVIDER_IDENTIFIER = "STARLINGBANK";
    private static final String DISPLAY_NAME = "Starling Bank";

    @Bean("StarlingBankObjectMapper")
    public ObjectMapper getObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(NON_NULL);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    @Bean("StarlingBankHttpClientFactoryV4")
    public StarlingBankHttpClientFactoryV4 httpClientFactoryV4(MeterRegistry meterRegistry,
                                                               StarlingBankProperties properties,
                                                               @Qualifier("StarlingBankObjectMapper") ObjectMapper objectMapper) {
        return new StarlingBankHttpClientFactoryV4(meterRegistry, objectMapper, properties, new StarlingBankHttpErrorHandler());
    }

    @Bean("StarlingBankAisAuthorizationServiceV5")
    public StarlingBankAuthorizationServiceV5 aisAuthorizationServiceV5(StarlingBankProperties properties,
                                                                        @Qualifier("StarlingBankObjectMapper") ObjectMapper objectMapper,
                                                                        Clock clock) {
        return new StarlingBankAuthorizationServiceV5(properties,
                new StarlingBankTokenMapper(objectMapper, clock),
                new StarlingBankAisAuthorizationUrlParametersProducer());
    }

    @Bean("StarlingBankPisAuthorizationServiceV5")
    public StarlingBankAuthorizationServiceV5 pisAuthorizationServiceV5(StarlingBankProperties properties,
                                                                        @Qualifier("StarlingBankObjectMapper") ObjectMapper objectMapper,
                                                                        Clock clock) {
        return new StarlingBankAuthorizationServiceV5(properties,
                new StarlingBankTokenMapper(objectMapper, clock),
                new StarlingBankPisAuthorizationUrlParametersProducer());
    }

    @Bean("StarlingBankFetchDataServiceV6")
    public StarlingBankFetchDataService fetchDataServiceV6(@Qualifier("StarlingBankObjectMapper") ObjectMapper objectMapper,
                                                           Clock clock) {
        return new StarlingBankFetchDataServiceV6(new StarlingBankTokenMapper(objectMapper, clock), clock);
    }

    @Bean("StarlingBankDataProviderV8")
    public UrlDataProvider getStarlingBankDataProviderV8(@Qualifier("StarlingBankObjectMapper") ObjectMapper objectMapper,
                                                         Clock clock,
                                                         StarlingBankHttpClientFactoryV4 httpClientFactoryV4,
                                                         @Qualifier("StarlingBankAisAuthorizationServiceV5") StarlingBankAuthorizationServiceV5 authorizationServiceV5,
                                                         StarlingBankAisAuthMeansSupplierV2 aisAuthMeansSupplierV2) {
        StarlingBankFetchDataService fetchDataServiceV6 = fetchDataServiceV6(objectMapper, clock);
        return new StarlingBankDataProvider(
                httpClientFactoryV4,
                authorizationServiceV5,
                fetchDataServiceV6,
                aisAuthMeansSupplierV2,
                clock,
                PROVIDER_IDENTIFIER,
                DISPLAY_NAME,
                ProviderVersion.VERSION_8
        );
    }

    @Bean("StarlingBankPaymentProviderV7")
    public StarlingBankPaymentProviderV2 getStarlingBankPaymentProviderV7(MeterRegistry meterRegistry,
                                                                          StarlingBankProperties properties,
                                                                          @Qualifier("StarlingBankPisAuthorizationServiceV5") StarlingBankAuthorizationServiceV5 authorizationServiceV5,
                                                                          StarlingBankPisAuthMeansSupplierV2 pisAuthMeansSupplierV2,
                                                                          @Qualifier("StarlingBankObjectMapper") ObjectMapper objectMapper,
                                                                          Clock clock) {
        UkInitiatePaymentExecutionContextAdapter<String, String, StarlingBankInitiatePaymentExecutionContextPreExecutionResult> initiatePaymentExecutionContextAdapter = UkInitiatePaymentExecutionContextAdapterBuilder.
                <String, String, StarlingBankInitiatePaymentExecutionContextPreExecutionResult>builder(
                new StarlingBankInitiatePaymentStatusesExtractor(),
                new StarlingBankPaymentAuthorizationUrlExtractor(
                        authorizationServiceV5,
                        pisAuthMeansSupplierV2,
                        PROVIDER_IDENTIFIER
                ),
                new StarlingBankInitiatePaymentProviderStateExtractor(),
                objectMapper,
                clock,
                String.class
        )
                .withHttpRequestInvoker(new StarlingBankInitiatePaymentHttpRequestInvoker())
                .withUkDomesticInitiatePaymentPreExecutionResultMapper(new StarlingBankInitiatePaymentPreExecutorResultMapper())
                .build(new StarlingBankRawBankPaymentStatusMapper());

        StarlingBankHttpHeadersProducerFactory httpHeadersProducerFactory = new StarlingBankHttpHeadersProducerFactory(clock);
        StarlingBankHttpClientFactoryV4 httpClientFactory = new StarlingBankHttpClientFactoryV4(meterRegistry, objectMapper, properties, new StarlingBankPaymentHttpErrorHandler());


        UkSubmitPaymentExecutionContextAdapter<PaymentRequest, PaymentSubmissionResponse, StarlingBankSubmitAndStatusPaymentExecutionContextPreExecutionResult> submitPaymentExecutionContextAdapter = UkSubmitPaymentExecutionContextAdapterBuilder.
                <PaymentRequest, PaymentSubmissionResponse, StarlingBankSubmitAndStatusPaymentExecutionContextPreExecutionResult>builder(
                        new StarlingBankSubmitPaymentStatusesExtractor(),
                        new StarlingBankPaymentIdExtractor(),
                        new StarlingBankSubmitPaymentProviderStateExtractor(),
                        objectMapper,
                        clock,
                        PaymentSubmissionResponse.class
                )
                .withHttpHeadersProvider(new StarlingBankSubmitPaymentHttpHeadersProvider(clock))
                .withHttpRequestBodyProvider(new StarlingBankSubmitPaymentHttpRequestBodyProvider())
                .withHttpRequestInvoker(new StarlingBankSubmitPaymentHttpRequestInvoker())
                .withResponseBodyValidator(new StarlingBankSubmitPaymentExecutionContextResponseBodyValidator())
                .withUkDomesticSubmitPreExecutionResultMapper(new StarlingBankSubmitPaymentPreExecutorResultMapper(
                        authorizationServiceV5,
                        objectMapper,
                        httpHeadersProducerFactory,
                        httpClientFactory,
                        pisAuthMeansSupplierV2,
                        DISPLAY_NAME,
                        PROVIDER_IDENTIFIER,
                        clock
                ))
                .buildWithCustomPreExecutionErrorHandler(new StarlingPreExecutionErrorHandler(
                                new DefaultPaymentExecutionContextPreExecutionErrorHandler(PaymentExecutionTechnicalException::paymentSubmissionException), clock),
                        new StarlingBankRawBankPaymentStatusMapper());

        UkStatusPaymentExecutionContextAdapter<Object, PaymentStatusResponse, StarlingBankSubmitAndStatusPaymentExecutionContextPreExecutionResult> statusPaymentExecutionContextAdapter = UkStatusPaymentExecutionContextAdapterBuilder.
                builder(
                        new StarlingBankStatusPaymentStatusesExtractor(new StarlingBankCommonPaymentStatusMapper()),
                        new StarlingBankStatusPaymentIdExtractor(),
                        new StarlingBankStatusPaymentProviderStateExtractor(),
                        objectMapper,
                        clock,
                        PaymentStatusResponse.class
                )
                .withUkDomesticStatusPaymentPreExecutionResultMapper(new StarlingBankStatusPaymentPreExecutorResultMapper(
                        authorizationServiceV5,
                        objectMapper,
                        httpHeadersProducerFactory,
                        httpClientFactory,
                        pisAuthMeansSupplierV2,
                        DISPLAY_NAME,
                        PROVIDER_IDENTIFIER,
                        clock
                ))
                .withHttpHeadersProvider(new StarlingBankStatusPaymentHttpHeadersProvider(
                        clock
                ))
                .withHttpRequestInvoker(new StarlingBankStatusPaymentHttpRequestInvoker())
                .withResponseBodyValidator(new StarlingBankStatusPaymentExecutionContextResponseBodyValidator())
                .buildWithCustomPreExecutionErrorHandler(new StarlingPreExecutionErrorHandler(
                                new DefaultPaymentExecutionContextPreExecutionErrorHandler(PaymentExecutionTechnicalException::paymentSubmissionException), clock),
                        new StarlingBankRawBankPaymentStatusMapper());

        return new StarlingBankPaymentProviderV2(
                initiatePaymentExecutionContextAdapter,
                submitPaymentExecutionContextAdapter,
                statusPaymentExecutionContextAdapter,
                PROVIDER_IDENTIFIER,
                DISPLAY_NAME,
                ProviderVersion.VERSION_7
        );
    }
}
