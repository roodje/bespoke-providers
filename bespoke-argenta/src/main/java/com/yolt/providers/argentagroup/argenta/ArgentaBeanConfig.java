package com.yolt.providers.argentagroup.argenta;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.yolt.providers.argentagroup.common.ArgentaGroupDataProvider;
import com.yolt.providers.argentagroup.common.exception.FetchDataHttpErrorHandler;
import com.yolt.providers.argentagroup.common.exception.TokensEndpointHttpErrorHandler;
import com.yolt.providers.argentagroup.common.http.DefaultHttpClientFactory;
import com.yolt.providers.argentagroup.common.http.JsonBodyDigestProducer;
import com.yolt.providers.argentagroup.common.http.SignatureProducer;
import com.yolt.providers.argentagroup.common.http.TppSignatureCertificateHeaderProducer;
import com.yolt.providers.argentagroup.common.service.CommonHttpHeadersProvider;
import com.yolt.providers.argentagroup.common.service.DefaultAuthenticationMeans;
import com.yolt.providers.argentagroup.common.service.ProviderStateMapper;
import com.yolt.providers.argentagroup.common.service.consent.*;
import com.yolt.providers.argentagroup.common.service.fetchdata.*;
import com.yolt.providers.argentagroup.common.service.token.*;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.rest.tracing.ExternalTracingUtil;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.securityutils.oauth2.OAuth2ProofKeyCodeExchange;
import io.micrometer.core.instrument.MeterRegistry;
import nl.ing.lovebird.extendeddata.account.BalanceType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class ArgentaBeanConfig {

    private static final String PROVIDER_IDENTIFIER = "ARGENTA";
    private static final String PROVIDER_IDENTIFIER_DISPLAY_NAME = "Argenta";
    private static final String INITIATE_CONSENT_ENDPOINT_PATH = "/berlingroup/v1/consents";
    private static final String DELETE_CONSENT_ENDPOINT_PATH = "/berlingroup/v1/consents/{consentId}";
    private static final String TOKENS_ENDPOINT_PATH = "/psd2/v1/berlingroup-auth/token";
    private static final String ACCOUNTS_ENDPOINT_PATH = "/berlingroup/v1/accounts";
    private static final String ACCOUNT_BALANCES_ENDPOINT_PATH = "/berlingroup/v1/accounts/{accountId}/balances";
    private static final String ACCOUNT_TRANSACTIONS_ENDPOINT_PATH = "/berlingroup/v1/accounts/{accountId}/transactions";


    @Bean("argentaDataProviderV1")
    public ArgentaGroupDataProvider getArgentaDataProviderV1(final MeterRegistry meterRegistry,
                                                             final ArgentaProperties properties,
                                                             final Clock clock) {
        ObjectMapper argentaObjectMapper = getArgentaObjectMapper();
        FetchDataHttpHeadersProvider fetchDataHttpHeadersProvider = new FetchDataHttpHeadersProvider(
                new CommonHttpHeadersProvider(
                        ExternalTracingUtil::createLastExternalTraceId,
                        new JsonBodyDigestProducer(argentaObjectMapper),
                        new SignatureProducer(),
                        new TppSignatureCertificateHeaderProducer()
                )
        );

        return new ArgentaGroupDataProvider(
                PROVIDER_IDENTIFIER,
                PROVIDER_IDENTIFIER_DISPLAY_NAME,
                ProviderVersion.VERSION_1,
                DefaultAuthenticationMeans.getTypedAuthenticationMeans(),
                ConsentValidityRules.EMPTY_RULES_SET,
                DefaultAuthenticationMeans::fromAuthMeans,
                new DefaultHttpClientFactory(
                        argentaObjectMapper,
                        meterRegistry,
                        properties
                ),
                new DefaultAuthorizationService(
                        clock,
                        TOKENS_ENDPOINT_PATH,
                        new CreateAccessMeansRequestBodyProvider(),
                        new RefreshAccessMeansRequestBodyProvider(),
                        new AccessMeansHttpHeadersProvider(),
                        new ProviderStateMapper(argentaObjectMapper),
                        new AccessMeansMapper(argentaObjectMapper),
                        new AuthorizationCodeExtractor(),
                        new TokensEndpointHttpErrorHandler()
                ),
                new DefaultConsentService(
                        INITIATE_CONSENT_ENDPOINT_PATH,
                        DELETE_CONSENT_ENDPOINT_PATH,
                        new InitiateConsentRequestBodyProvider(clock),
                        new InitiateConsentHttpHeadersProvider(
                                new CommonHttpHeadersProvider(
                                        ExternalTracingUtil::createLastExternalTraceId,
                                        new JsonBodyDigestProducer(argentaObjectMapper),
                                        new SignatureProducer(),
                                        new TppSignatureCertificateHeaderProducer()
                                )
                        ),
                        new InitiateConsentResponseMapper(),
                        new AuthorizationUrlEnricher(),
                        OAuth2ProofKeyCodeExchange::createRandomS256,
                        new DeleteConsentHttpHeadersProvider(
                                ExternalTracingUtil::createLastExternalTraceId,
                                new JsonBodyDigestProducer(argentaObjectMapper),
                                new SignatureProducer(),
                                new TppSignatureCertificateHeaderProducer()
                        ),
                        new ProviderStateMapper(argentaObjectMapper),
                        new AccessMeansMapper(argentaObjectMapper),
                        new TokensEndpointHttpErrorHandler()
                ),
                new DefaultFetchDataService(
                        BalanceType.INTERIM_AVAILABLE,
                        BalanceType.INTERIM_BOOKED,
                        new AccessMeansMapper(argentaObjectMapper),
                        new DefaultAccountsDataFetchService(
                                ACCOUNTS_ENDPOINT_PATH,
                                fetchDataHttpHeadersProvider,
                                new FetchDataHttpErrorHandler(),
                                new AccountsMapper(clock)
                        ),
                        new DefaultBalancesDataFetchService(
                                ACCOUNT_BALANCES_ENDPOINT_PATH,
                                fetchDataHttpHeadersProvider,
                                new FetchDataHttpErrorHandler(),
                                new BalancesMapper()
                        ),
                        new DefaultTransactionsDataFetchService(
                                ACCOUNT_TRANSACTIONS_ENDPOINT_PATH,
                                properties,
                                fetchDataHttpHeadersProvider,
                                new TransactionsMapper(ZoneId.of("Europe/Brussels")),
                                new FetchDataHttpErrorHandler()
                        )
                )
        );
    }

    @Bean
    @Qualifier("argentaObjectMapper")
    public ObjectMapper getArgentaObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        mapper.registerModule(new JavaTimeModule());

        return mapper;
    }
}
