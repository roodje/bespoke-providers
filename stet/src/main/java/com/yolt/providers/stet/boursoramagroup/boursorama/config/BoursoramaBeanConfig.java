package com.yolt.providers.stet.boursoramagroup.boursorama.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.stet.boursoramagroup.boursorama.BoursoramaDataProviderV4;
import com.yolt.providers.stet.boursoramagroup.boursorama.auth.BoursoramaAuthenticationMeansSupplier;
import com.yolt.providers.stet.boursoramagroup.common.BoursoramaProviderStateMapper;
import com.yolt.providers.stet.boursoramagroup.common.http.BoursoramaGroupSignatureStrategy;
import com.yolt.providers.stet.boursoramagroup.common.http.error.BoursoramaHttpErrorHandler;
import com.yolt.providers.stet.boursoramagroup.common.mapper.account.BoursoramaGroupAccountMapper;
import com.yolt.providers.stet.boursoramagroup.common.mapper.token.BoursoramaGroupTokenRequestMapper;
import com.yolt.providers.stet.boursoramagroup.common.service.BoursoramaGroupAuthorizationRedirectUrlSupplier;
import com.yolt.providers.stet.boursoramagroup.common.service.authorization.tool.BoursoramaGroupAuthorizationCodeExtractor;
import com.yolt.providers.stet.boursoramagroup.common.service.fetchdata.BoursoramaGroupFetchDataService;
import com.yolt.providers.stet.boursoramagroup.common.service.fetchdata.transaction.BoursoramaGroupFetchTransactionsStrategy;
import com.yolt.providers.stet.boursoramagroup.common.service.pec.BoursoramaGroupGenericPaymentProviderV2Factory;
import com.yolt.providers.stet.boursoramagroup.common.service.pec.BoursoramaGroupSigningPaymentHttpHeadersFactory;
import com.yolt.providers.stet.boursoramagroup.common.service.pec.authorization.BoursoramaGroupPaymentAuthorizationUrlExtractor;
import com.yolt.providers.stet.boursoramagroup.common.service.rest.BoursoramaGroupAuthorizationHttpHeadersFactory;
import com.yolt.providers.stet.boursoramagroup.common.service.rest.BoursoramaGroupAuthorizationRestClientV2;
import com.yolt.providers.stet.boursoramagroup.common.service.rest.BoursoramaGroupFetchDataRestClient;
import com.yolt.providers.stet.generic.GenericPaymentProviderV2;
import com.yolt.providers.stet.generic.auth.keyrequirements.DefaultKeyRequirementsProducer;
import com.yolt.providers.stet.generic.domain.DigestAlgorithm;
import com.yolt.providers.stet.generic.domain.ProviderIdentification;
import com.yolt.providers.stet.generic.domain.Scope;
import com.yolt.providers.stet.generic.http.client.DefaultHttpClientFactory;
import com.yolt.providers.stet.generic.http.error.NoActionHttpErrorHandler;
import com.yolt.providers.stet.generic.http.signer.DefaultHttpSigner;
import com.yolt.providers.stet.generic.http.signer.HttpSigner;
import com.yolt.providers.stet.generic.mapper.DateTimeSupplier;
import com.yolt.providers.stet.generic.mapper.transaction.DefaultTransactionMapper;
import com.yolt.providers.stet.generic.service.authorization.SingleRegionAuthorizationService;
import com.yolt.providers.stet.generic.service.authorization.refresh.RefreshTokenSupportedStrategy;
import com.yolt.providers.stet.generic.service.authorization.tool.ConsentValidityRulesBuilder;
import com.yolt.providers.stet.generic.service.fetchdata.account.FetchBasicAccountsStrategy;
import com.yolt.providers.stet.generic.service.fetchdata.balance.FetchBasicBalancesStrategy;
import com.yolt.providers.stet.generic.service.fetchdata.rest.DefaultFetchDataRestClient;
import com.yolt.providers.stet.generic.service.fetchdata.rest.header.FetchDataSigningHttpHeadersFactory;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration("BoursoramaStetBeanConfig")
public class BoursoramaBeanConfig {

    private static final String PROVIDER_IDENTIFIER = "BOURSORAMA";
    private static final String PROVIDER_DISPLAY_NAME = "Boursorama";

    @Bean("BoursoramaDataProviderV4")
    public BoursoramaDataProviderV4 createBoursoramaDataProviderV4(MeterRegistry meterRegistry,
                                                                   BoursoramaProperties properties,
                                                                   Clock clock,
                                                                   @Qualifier("StetObjectMapper") ObjectMapper objectMapper) {
        BoursoramaAuthenticationMeansSupplier authMeansSupplier = new BoursoramaAuthenticationMeansSupplier(new DefaultKeyRequirementsProducer());
        HttpSigner httpSigner = createHttpSigner(objectMapper);

        BoursoramaGroupAuthorizationRestClientV2 authorizationRestClient = new BoursoramaGroupAuthorizationRestClientV2(
                new BoursoramaGroupAuthorizationHttpHeadersFactory(objectMapper, httpSigner),
                new BoursoramaGroupTokenRequestMapper(),
                new BoursoramaHttpErrorHandler()
        );

        DefaultFetchDataRestClient fetchDataRestClient = new BoursoramaGroupFetchDataRestClient(new FetchDataSigningHttpHeadersFactory(
                httpSigner));

        DateTimeSupplier dateTimeSupplier = new DateTimeSupplier(clock);

        BoursoramaProviderStateMapper providerStateMapper = new BoursoramaProviderStateMapper(objectMapper, properties);

        return new BoursoramaDataProviderV4(
                authMeansSupplier,
                new DefaultHttpClientFactory(meterRegistry, objectMapper),
                new SingleRegionAuthorizationService(
                        new RefreshTokenSupportedStrategy(
                                providerStateMapper,
                                Scope.AISP_EXTENDED_TRANSACTION_HISTORY,
                                authorizationRestClient,
                                dateTimeSupplier),
                        authorizationRestClient,
                        providerStateMapper,
                        Scope.AISP_EXTENDED_TRANSACTION_HISTORY,
                        properties,
                        new BoursoramaGroupAuthorizationCodeExtractor(),
                        new BoursoramaGroupAuthorizationRedirectUrlSupplier(),
                        dateTimeSupplier),
                new BoursoramaGroupFetchDataService(
                        fetchDataRestClient,
                        providerStateMapper,
                        new FetchBasicAccountsStrategy(fetchDataRestClient),
                        new BoursoramaGroupFetchTransactionsStrategy(fetchDataRestClient, dateTimeSupplier, properties),
                        new FetchBasicBalancesStrategy(fetchDataRestClient),
                        dateTimeSupplier,
                        new BoursoramaGroupAccountMapper(dateTimeSupplier),
                        new DefaultTransactionMapper(dateTimeSupplier),
                        properties),
                providerStateMapper);
    }

    @Bean("BoursoramaPaymentProviderV3")
    public GenericPaymentProviderV2 createBoursoramaPaymentProviderV3(MeterRegistry meterRegistry,
                                                                      BoursoramaProperties properties,
                                                                      Clock clock,
                                                                      @Qualifier("StetObjectMapper") ObjectMapper objectMapper) {
        ObjectMapper boursoramaObjectMapper = objectMapper.copy();
        return new BoursoramaGroupGenericPaymentProviderV2Factory(
                new ProviderIdentification(
                        PROVIDER_IDENTIFIER,
                        PROVIDER_DISPLAY_NAME,
                        ProviderVersion.VERSION_3),
                new BoursoramaGroupPaymentAuthorizationUrlExtractor(),
                new BoursoramaGroupSigningPaymentHttpHeadersFactory(
                        createHttpSigner(boursoramaObjectMapper),
                        boursoramaObjectMapper),
                new DefaultHttpClientFactory(meterRegistry, boursoramaObjectMapper),
                new NoActionHttpErrorHandler(),
                new BoursoramaAuthenticationMeansSupplier(new DefaultKeyRequirementsProducer()),
                new DateTimeSupplier(clock),
                new BoursoramaProviderStateMapper(boursoramaObjectMapper, properties),
                properties,
                ConsentValidityRulesBuilder.consentPageRules()
                        .containsKeyword("Mon identifiant")
                        .containsKeyword("Saisissez votre identifiant")
                        .containsKeyword("Suivant")
                        .build(),
                Scope.PISP,
                boursoramaObjectMapper,
                clock)
                .createPaymentProvider();
    }

    private HttpSigner createHttpSigner(ObjectMapper objectMapper) {
        BoursoramaGroupSignatureStrategy signatureStrategy = new BoursoramaGroupSignatureStrategy(SignatureAlgorithm.SHA256_WITH_RSA);
        return new DefaultHttpSigner(signatureStrategy, objectMapper, DigestAlgorithm.SHA_256);
    }
}
