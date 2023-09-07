package com.yolt.providers.stet.lclgroup.lcl.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.stet.generic.GenericPaymentProviderV3;
import com.yolt.providers.stet.generic.auth.keyrequirements.DefaultKeyRequirementsProducer;
import com.yolt.providers.stet.generic.domain.DigestAlgorithm;
import com.yolt.providers.stet.generic.domain.ProviderIdentification;
import com.yolt.providers.stet.generic.domain.Scope;
import com.yolt.providers.stet.generic.http.client.DefaultHttpClientFactory;
import com.yolt.providers.stet.generic.http.client.DefaultHttpClientFactoryV2;
import com.yolt.providers.stet.generic.http.error.NoActionHttpErrorHandlerV2;
import com.yolt.providers.stet.generic.http.signer.DefaultHttpSigner;
import com.yolt.providers.stet.generic.http.signer.HttpSigner;
import com.yolt.providers.stet.generic.mapper.DateTimeSupplier;
import com.yolt.providers.stet.generic.service.authorization.SingleRegionAuthorizationService;
import com.yolt.providers.stet.generic.service.authorization.refresh.RefreshTokenSupportedStrategy;
import com.yolt.providers.stet.generic.service.authorization.rest.DefaultAuthorizationRestClient;
import com.yolt.providers.stet.generic.service.authorization.rest.header.DefaultAuthorizationNoSigningHttpHeadersFactory;
import com.yolt.providers.stet.generic.service.authorization.tool.ConsentValidityRulesBuilder;
import com.yolt.providers.stet.generic.service.fetchdata.account.FetchBasicAccountsStrategy;
import com.yolt.providers.stet.lclgroup.common.auth.*;
import com.yolt.providers.stet.lclgroup.common.errorhandling.LclHttpErrorHandler;
import com.yolt.providers.stet.lclgroup.common.fetchdata.*;
import com.yolt.providers.stet.lclgroup.common.onboarding.LclGroupAutoOnBoardingService;
import com.yolt.providers.stet.lclgroup.common.pec.LclGroupGenericPaymentProviderV2Factory;
import com.yolt.providers.stet.lclgroup.common.pec.authorization.LclGroupPaymentAuthorizationUrlExtractor;
import com.yolt.providers.stet.lclgroup.common.pec.authorization.token.LclGroupSigningPaymentHttpHeadersFactory;
import com.yolt.providers.stet.lclgroup.common.pec.initiate.LclGroupPaymentHeadersExtractor;
import com.yolt.providers.stet.lclgroup.common.service.authorization.tool.LclGroupRedirectUrlSupplier;
import com.yolt.providers.stet.lclgroup.lcl.LclDataProviderV3;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.Period;

import static com.yolt.providers.common.versioning.ProviderVersion.VERSION_3;

@Configuration
public class LclStetBeanConfig {

    private static final String PROVIDER_IDENTIFIER = "LCL";
    private static final ProviderVersion VERSION = VERSION_3;

    @Bean("LclDataProviderV3")
    public LclDataProviderV3 createLclDataProviderV3(LclStetProperties properties,
                                                     MeterRegistry meterRegistry,
                                                     Clock clock,
                                                     @Qualifier("StetObjectMapper") ObjectMapper objectMapper) {
        DefaultHttpSigner signer = new DefaultHttpSigner(new LclCavageSignatureStrategy(SignatureAlgorithm.SHA256_WITH_RSA), objectMapper, DigestAlgorithm.SHA_256);
        LclGroupAuthenticationMeansSupplier authMeansSupplier = new LclGroupAuthenticationMeansSupplier(new DefaultKeyRequirementsProducer());
        LclHttpErrorHandler errorHandler = new LclHttpErrorHandler();
        DefaultAuthorizationRestClient authorizationRestClient = new DefaultAuthorizationRestClient(
                new DefaultAuthorizationNoSigningHttpHeadersFactory(),
                new LclTokenRequestMapper(),
                errorHandler);
        LclFetchDataRestClient fetchDataRestClient = new LclFetchDataRestClient(
                new LclFetchDataSigningHttpHeadersFactory(
                        signer,
                        properties,
                        PROVIDER_IDENTIFIER,
                        clock),
                errorHandler);
        LclDateTimeSupplier dateTimeSupplier = new LclDateTimeSupplier(clock);
        LclProviderStateMapper providerStateMapper = new LclProviderStateMapper(properties, objectMapper);
        return new LclDataProviderV3(
                authMeansSupplier,
                new DefaultHttpClientFactory(meterRegistry, objectMapper),
                new SingleRegionAuthorizationService(
                        new RefreshTokenSupportedStrategy(
                                providerStateMapper,
                                Scope.AISP,
                                authorizationRestClient,
                                dateTimeSupplier),
                        authorizationRestClient,
                        providerStateMapper,
                        Scope.AISP_EXTENDED_TRANSACTION_HISTORY,
                        properties,
                        new LclAuthorizationCodeExtractor(),
                        new LclGroupRedirectUrlSupplier(),
                        dateTimeSupplier),
                new LclFechtDataService(
                        fetchDataRestClient,
                        providerStateMapper,
                        new FetchBasicAccountsStrategy(fetchDataRestClient),
                        new LclFetchTransactionsStrategy(
                                fetchDataRestClient,
                                dateTimeSupplier,
                                properties,
                                Period.ofDays(89)),
                        new LclFetchBalanceStrategy(fetchDataRestClient),
                        dateTimeSupplier,
                        new LclAccountMapper(dateTimeSupplier),
                        new LclTransactionMapper(dateTimeSupplier),
                        properties),
                providerStateMapper,
                new LclGroupAutoOnBoardingService(),
                properties,
                ConsentValidityRulesBuilder.emptyRules(),
                PROVIDER_IDENTIFIER,
                PROVIDER_IDENTIFIER,
                VERSION
        );
    }

    @Bean("LclPaymentProviderV2")
    public GenericPaymentProviderV3 createLclPaymentProviderV2(MeterRegistry meterRegistry,
                                                               LclStetProperties properties,
                                                               Clock clock,
                                                               @Qualifier("StetObjectMapper") ObjectMapper objectMapper) {
        ObjectMapper lclObjectMapper = objectMapper.copy();
        return new LclGroupGenericPaymentProviderV2Factory(
                new ProviderIdentification(
                        PROVIDER_IDENTIFIER,
                        PROVIDER_IDENTIFIER,
                        ProviderVersion.VERSION_2),
                new LclGroupPaymentAuthorizationUrlExtractor(),
                new LclGroupSigningPaymentHttpHeadersFactory(createHttpSigner(objectMapper), clock),
                new DefaultHttpClientFactoryV2(meterRegistry, lclObjectMapper),
                new NoActionHttpErrorHandlerV2(),
                new LclGroupAuthenticationMeansSupplier(new DefaultKeyRequirementsProducer()),
                new DateTimeSupplier(clock),
                properties,
                ConsentValidityRulesBuilder.emptyRules(),
                Scope.PISP,
                lclObjectMapper,
                clock,
                new LclGroupPaymentHeadersExtractor())
                .createPaymentProvider();
    }

    private HttpSigner createHttpSigner(ObjectMapper objectMapper) {
        LclCavageSignatureForPisStrategy signatureStrategy = new LclCavageSignatureForPisStrategy(SignatureAlgorithm.SHA256_WITH_RSA);
        return new DefaultHttpSigner(signatureStrategy, objectMapper, DigestAlgorithm.SHA_256);
    }
}