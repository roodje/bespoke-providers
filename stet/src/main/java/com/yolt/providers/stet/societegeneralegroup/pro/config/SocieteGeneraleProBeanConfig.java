package com.yolt.providers.stet.societegeneralegroup.pro.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.stet.generic.GenericPaymentProviderV3;
import com.yolt.providers.stet.generic.auth.keyrequirements.DefaultKeyRequirementsProducer;
import com.yolt.providers.stet.generic.domain.DigestAlgorithm;
import com.yolt.providers.stet.generic.domain.ProviderIdentification;
import com.yolt.providers.stet.generic.domain.Scope;
import com.yolt.providers.stet.generic.http.client.DefaultHttpClientFactory;
import com.yolt.providers.stet.generic.http.client.DefaultHttpClientFactoryV2;
import com.yolt.providers.stet.generic.http.error.DefaultHttpErrorHandler;
import com.yolt.providers.stet.generic.http.error.NoActionHttpErrorHandlerV2;
import com.yolt.providers.stet.generic.http.signer.DefaultHttpSigner;
import com.yolt.providers.stet.generic.http.signer.HttpSigner;
import com.yolt.providers.stet.generic.service.authorization.SingleRegionAuthorizationService;
import com.yolt.providers.stet.generic.service.authorization.refresh.RefreshTokenSupportedStrategy;
import com.yolt.providers.stet.generic.service.authorization.rest.DefaultAuthorizationRestClient;
import com.yolt.providers.stet.generic.service.authorization.tool.DefaultAuthorizationCodeExtractor;
import com.yolt.providers.stet.generic.service.authorization.tool.DefaultAuthorizationRedirectUrlSupplier;
import com.yolt.providers.stet.generic.service.fetchdata.account.FetchBasicAccountsStrategy;
import com.yolt.providers.stet.generic.service.fetchdata.rest.DefaultFetchDataRestClient;
import com.yolt.providers.stet.societegeneralegroup.common.SocieteGeneraleGroupDataProvider;
import com.yolt.providers.stet.societegeneralegroup.common.auth.SocieteGeneraleAuthenticationMeansSupplier;
import com.yolt.providers.stet.societegeneralegroup.common.http.signer.signature.SocieteGeneraleGroupSignatureStrategy;
import com.yolt.providers.stet.societegeneralegroup.common.mapper.SocieteGeneraleDateTimeSupplier;
import com.yolt.providers.stet.societegeneralegroup.common.mapper.account.SocieteGeneraleAccountMapper;
import com.yolt.providers.stet.societegeneralegroup.common.mapper.providerstate.SocieteGeneraleGroupStateMapper;
import com.yolt.providers.stet.societegeneralegroup.common.mapper.token.SocieteGeneraleGroupTokenRequestMapper;
import com.yolt.providers.stet.societegeneralegroup.common.mapper.transaction.SocieteGeneraleTransactionMapper;
import com.yolt.providers.stet.societegeneralegroup.common.service.authorization.rest.header.SocieteGeneraleGroupAuthorizationNoSigningHttpHeadersFactory;
import com.yolt.providers.stet.societegeneralegroup.common.service.fetchdata.SocieteGeneraleGroupFetchDataService;
import com.yolt.providers.stet.societegeneralegroup.common.service.fetchdata.balance.SocieteGeneraleGroupFetchBasicBalancesStrategy;
import com.yolt.providers.stet.societegeneralegroup.common.service.fetchdata.rest.header.SocieteGeneraleGroupFetchDataSigningHttpHeadersFactory;
import com.yolt.providers.stet.societegeneralegroup.common.service.fetchdata.transaction.SocieteGeneraleGroupFetchTransactionsStrategy;
import com.yolt.providers.stet.societegeneralegroup.common.service.pec.SocieteGeneraleGroupPaymentProviderFactory;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration("SocieteGeneraleProBeanConfig")
public class SocieteGeneraleProBeanConfig {

    private static final String PROVIDER_IDENTIFIER = "SOCIETE_GENERALE_PRO";
    private static final String PROVIDER_DISPLAY_NAME = "Societe Generale Professionnels";

    @Bean("SocieteGeneraleProDataProviderV5")
    public SocieteGeneraleGroupDataProvider SocieteGeneralePriDataProviderV5(MeterRegistry meterRegistry,
                                                                             Clock clock,
                                                                             @Qualifier("SocieteGeneraleProStetProperties") SocieteGeneraleProProperties properties,
                                                                             @Qualifier("StetObjectMapper") ObjectMapper objectMapper) {
        SocieteGeneraleAuthenticationMeansSupplier authMeansSupplier = new SocieteGeneraleAuthenticationMeansSupplier(new DefaultKeyRequirementsProducer());
        SocieteGeneraleGroupStateMapper providerStateMapper = new SocieteGeneraleGroupStateMapper(objectMapper, properties);
        DefaultAuthorizationRestClient defaultAuthorizationRestClient = new DefaultAuthorizationRestClient(
                new SocieteGeneraleGroupAuthorizationNoSigningHttpHeadersFactory(),
                new SocieteGeneraleGroupTokenRequestMapper(),
                new DefaultHttpErrorHandler());
        SocieteGeneraleDateTimeSupplier dateTimeSupplier = new SocieteGeneraleDateTimeSupplier(clock);
        SingleRegionAuthorizationService singleRegionAuthorizationService = new SingleRegionAuthorizationService(
                new RefreshTokenSupportedStrategy(providerStateMapper, Scope.AISP, defaultAuthorizationRestClient, dateTimeSupplier),
                defaultAuthorizationRestClient,
                providerStateMapper,
                Scope.AISP,
                properties,
                new DefaultAuthorizationCodeExtractor(),
                new DefaultAuthorizationRedirectUrlSupplier(),
                dateTimeSupplier);

        HttpSigner httpSigner = new DefaultHttpSigner(new SocieteGeneraleGroupSignatureStrategy(SignatureAlgorithm.SHA256_WITH_RSA, properties),
                objectMapper,
                DigestAlgorithm.SHA_256);

        DefaultFetchDataRestClient defaultFetchDataRestClient = new DefaultFetchDataRestClient(
                new SocieteGeneraleGroupFetchDataSigningHttpHeadersFactory(httpSigner));

        SocieteGeneraleGroupFetchDataService defaultFetchDataService = new SocieteGeneraleGroupFetchDataService(
                defaultFetchDataRestClient,
                providerStateMapper,
                new FetchBasicAccountsStrategy(defaultFetchDataRestClient),
                new SocieteGeneraleGroupFetchTransactionsStrategy(defaultFetchDataRestClient, dateTimeSupplier, properties),
                new SocieteGeneraleGroupFetchBasicBalancesStrategy(),
                dateTimeSupplier,
                new SocieteGeneraleAccountMapper(dateTimeSupplier),
                new SocieteGeneraleTransactionMapper(dateTimeSupplier),
                properties);

        DefaultHttpClientFactory defaultHttpClientFactory = new DefaultHttpClientFactory(meterRegistry, objectMapper);

        return new SocieteGeneraleGroupDataProvider(new ProviderIdentification(PROVIDER_IDENTIFIER, PROVIDER_DISPLAY_NAME, ProviderVersion.VERSION_5),
                authMeansSupplier,
                defaultHttpClientFactory,
                singleRegionAuthorizationService,
                defaultFetchDataService,
                providerStateMapper,
                ConsentValidityRules.EMPTY_RULES_SET
        );
    }

    @Bean("SocieteGeneraleProPaymentProviderV5")
    public GenericPaymentProviderV3 createSocieteGeneraleProPaymentProviderV5(MeterRegistry meterRegistry,
                                                                              Clock clock,
                                                                              @Qualifier("SocieteGeneraleProStetProperties") SocieteGeneraleProProperties properties,
                                                                              @Qualifier("StetObjectMapper") ObjectMapper objectMapper) {
        ObjectMapper societeGeneraleProObjectMapper = objectMapper.copy();
        return new SocieteGeneraleGroupPaymentProviderFactory(
                new ProviderIdentification(
                        PROVIDER_IDENTIFIER,
                        PROVIDER_DISPLAY_NAME,
                        ProviderVersion.VERSION_5),
                new SocieteGeneraleAuthenticationMeansSupplier(new DefaultKeyRequirementsProducer()),
                ConsentValidityRules.EMPTY_RULES_SET,
                societeGeneraleProObjectMapper,
                clock,
                properties,
                new DefaultHttpClientFactoryV2(meterRegistry, societeGeneraleProObjectMapper),
                new NoActionHttpErrorHandlerV2())
                .createPaymentProvider();
    }
}
