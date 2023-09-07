package com.yolt.providers.openbanking.ais.virginmoney2group.virginmoney2.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.openbanking.ais.common.HsmUtils;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.claims.producer.DefaultJwtClaimsProducer;
import com.yolt.providers.openbanking.ais.generic2.claims.token.DefaultTokenClaimsProducer;
import com.yolt.providers.openbanking.ais.generic2.common.OpenBankingTokenScope;
import com.yolt.providers.openbanking.ais.generic2.common.ProviderIdentification;
import com.yolt.providers.openbanking.ais.generic2.domain.LoginInfoState;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClientFactory;
import com.yolt.providers.openbanking.ais.generic2.oauth2.OneRefreshTokenDecorator;
import com.yolt.providers.openbanking.ais.generic2.service.AuthenticationService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.accountaccessconsentrequestservice.DefaultAccountAccessConsentRequestServiceV2;
import com.yolt.providers.openbanking.ais.generic2.service.ais.accountrequestservice.AccountRequestService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.accountrequestservice.DefaultConsentPermissions;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.DefaultAccountFilter;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.FetchDataServiceV2;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.supportedaccounts.DefaultSupportedAccountsSupplier;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account.*;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account.schemesupport.DefaultSupportedSchemeAccountFilter;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.accountrefferencetypemapper.DefaultAccountReferenceTypeMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.amount.DefaultAmountParser;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.balance.DefaultBalanceMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.balanceamount.DefaultBalanceAmountMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.balancetype.DefaultBalanceTypeMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.creditcard.CreditCardMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.currency.DefaultCurrencyMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.extendedaccount.DefaultExtendedAccountMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.extendedbalance.DefaultExtendedBalancesMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.extendedtransaction.DefaultExtendedTransactionMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.oauthtoken.DefaultAccessMeansStateProvider;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.oauthtoken.DefaultLoginInfoStateMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.parties.DefaultPartiesMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.period.DefaultPeriodMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.scheme.DefaultSchemeMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.time.DefaultDateTimeMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transaction.DefaultDirectDebitMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transaction.DefaultStandingOrderMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transaction.DefaultTransactionMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transactionstatus.DefaultTransactionStatusMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transactiontype.DefaultTransactionTypeMapper;
import com.yolt.providers.openbanking.ais.generic2.service.restclient.DefaultPartiesRestClient;
import com.yolt.providers.openbanking.ais.generic2.service.restclient.DefaultRestClient;
import com.yolt.providers.openbanking.ais.generic2.service.restclient.PartiesRestClient;
import com.yolt.providers.openbanking.ais.generic2.service.restclient.RestClient;
import com.yolt.providers.openbanking.ais.generic2.signer.ExternalPaymentRequestSigner;
import com.yolt.providers.openbanking.ais.generic2.signer.ExternalUserRequestTokenSigner;
import com.yolt.providers.openbanking.ais.virginmoney2group.common.VirginMoney2DataProviderV1;
import com.yolt.providers.openbanking.ais.virginmoney2group.common.auth.VirginMoney2GroupAuthMeansBuilder;
import com.yolt.providers.openbanking.ais.virginmoney2group.common.auth.VirginMoney2GroupClientSecretBasicOauth2Client;
import com.yolt.providers.openbanking.ais.virginmoney2group.common.http.VirginMoney2GroupHttpClientFactory;
import com.yolt.providers.openbanking.ais.virginmoney2group.common.mapper.VirginMoney2GroupAccessMeansStateMapper;
import com.yolt.providers.openbanking.ais.virginmoney2group.common.producer.VirginMoney2GroupTokenBodyProducer;
import com.yolt.providers.openbanking.ais.virginmoney2group.common.properties.VirginMoney2GroupProperties;
import com.yolt.providers.openbanking.ais.virginmoney2group.common.service.VirginMoney2GroupAuthenticationServiceV1;
import com.yolt.providers.openbanking.ais.virginmoney2group.common.service.VirginMoney2GroupAutoOnboardingService;
import com.yolt.providers.openbanking.ais.virginmoney2group.common.service.ais.fetchdataservice.VirginMoney2GroupFetchDataService;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBBalanceType1Code;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBReadConsent1Data;
import io.micrometer.core.instrument.MeterRegistry;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.Duration;
import java.time.ZoneId;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.yolt.providers.openbanking.ais.virginmoney2group.common.auth.VirginMoney2GroupAuthMeansBuilder.*;
import static com.yolt.providers.openbanking.dto.ais.openbanking316.OBBalanceType1Code.INTERIMAVAILABLE;
import static com.yolt.providers.openbanking.dto.ais.openbanking316.OBBalanceType1Code.INTERIMBOOKED;

@Configuration
public class VirginMoney2AisBeanConfig {

    private static final String PROVIDER_IDENTIFIER = "VIRGIN_MONEY_MERGED_APIS";
    private static final String PROVIDER_DISPLAY_NAME = "Virgin Money";
    private static final ZoneId ZONE_ID = ZoneId.of("Europe/London");
    private static final String ENDPOINT_VERSION = "/v3.1";
    private static final String JWS_ALGORITHM = AlgorithmIdentifiers.RSA_PSS_USING_SHA256;
    private static final List<OBReadConsent1Data.PermissionsEnum> CONSENT_PERMISSIONS = List.of(
            OBReadConsent1Data.PermissionsEnum.READPARTY,
            OBReadConsent1Data.PermissionsEnum.READACCOUNTSDETAIL,
            OBReadConsent1Data.PermissionsEnum.READBALANCES,
            OBReadConsent1Data.PermissionsEnum.READDIRECTDEBITS,
            OBReadConsent1Data.PermissionsEnum.READSTANDINGORDERSDETAIL,
            OBReadConsent1Data.PermissionsEnum.READTRANSACTIONSCREDITS,
            OBReadConsent1Data.PermissionsEnum.READTRANSACTIONSDEBITS,
            OBReadConsent1Data.PermissionsEnum.READTRANSACTIONSDETAIL);

    @Bean("VirginMoney2DataProviderV1")
    public VirginMoney2DataProviderV1 getVirginMoney2DataProviderV1(@Qualifier("OpenBanking") ObjectMapper objectMapper,
                                                                    VirginMoney2Properties properties,
                                                                    MeterRegistry meterRegistry,
                                                                    Clock clock) {
        RestClient restClient = getRestClient(objectMapper);
        TokenScope tokenScope = TokenScope.builder()
                .grantScope(OpenBankingTokenScope.ACCOUNTS.getAuthorizationUrlScope())
                .authorizationUrlScope(OpenBankingTokenScope.ACCOUNTS.getAuthorizationUrlScope())
                .build();
        AuthenticationService authenticationService = getAuthenticationService(properties, clock, tokenScope);
        HttpClientFactory httpClientFactory = getHttpClientFactory(properties, meterRegistry, objectMapper);
        Function<Map<String, BasicAuthenticationMean>, DefaultAuthMeans> getAuthenticationMeans = VirginMoney2GroupAuthMeansBuilder::createAuthenticationMeansForAis;
        ProviderIdentification providerIdentification = new ProviderIdentification(PROVIDER_IDENTIFIER, PROVIDER_DISPLAY_NAME, ProviderVersion.VERSION_1);

        return new VirginMoney2DataProviderV1(getFetchDataService(properties, restClient, getPartiesRestClient(), clock),
                getAccountRequestService(restClient, authenticationService),
                authenticationService,
                httpClientFactory,
                tokenScope,
                providerIdentification,
                getAuthenticationMeans,
                getTypedAuthenticationMeansSupplier(),
                new VirginMoney2GroupAccessMeansStateMapper(objectMapper),
                new DefaultAccessMeansStateProvider(),
                () -> HsmUtils.getKeyRequirements(SIGNING_PRIVATE_KEY_ID_NAME),
                () -> HsmUtils.getKeyRequirements(TRANSPORT_PRIVATE_KEY_ID_NAME, TRANSPORT_CERTIFICATE_NAME),
                () -> ConsentValidityRules.EMPTY_RULES_SET,
                new DefaultLoginInfoStateMapper<>(objectMapper),
                LoginInfoState::new,
                new DefaultConsentPermissions(CONSENT_PERMISSIONS),
                new VirginMoney2GroupAutoOnboardingService(properties,
                        httpClientFactory,
                        getAuthenticationMeans,
                        providerIdentification),
                clock);
    }

    private Supplier<Map<String, TypedAuthenticationMeans>> getTypedAuthenticationMeansSupplier() {
        Map<String, TypedAuthenticationMeans> typedAuthenticationMeans = new HashMap<>();
        typedAuthenticationMeans.put(INSTITUTION_ID_NAME, TypedAuthenticationMeans.INSTITUTION_ID_STRING);
        typedAuthenticationMeans.put(CLIENT_ID_NAME, TypedAuthenticationMeans.CLIENT_ID_STRING);
        typedAuthenticationMeans.put(CLIENT_SECRET_NAME, TypedAuthenticationMeans.CLIENT_SECRET_STRING);
        typedAuthenticationMeans.put(SIGNING_PRIVATE_KEY_ID_NAME, TypedAuthenticationMeans.KEY_ID);
        typedAuthenticationMeans.put(SIGNING_KEY_HEADER_ID_NAME, TypedAuthenticationMeans.SIGNING_KEY_ID_STRING);
        typedAuthenticationMeans.put(TRANSPORT_PRIVATE_KEY_ID_NAME, TypedAuthenticationMeans.KEY_ID);
        typedAuthenticationMeans.put(TRANSPORT_CERTIFICATE_NAME, TypedAuthenticationMeans.CERTIFICATE_PEM);
        typedAuthenticationMeans.put(SOFTWARE_STATEMENT_ASSERTION_NAME, TypedAuthenticationMeans.SOFTWARE_STATEMENT_ASSERTION_STRING);
        typedAuthenticationMeans.put(SOFTWARE_ID_NAME, TypedAuthenticationMeans.SOFTWARE_ID_STRING);
        return () -> typedAuthenticationMeans;
    }

    private HttpClientFactory getHttpClientFactory(VirginMoney2GroupProperties properties,
                                                   MeterRegistry meterRegistry,
                                                   ObjectMapper objectMapper) {
        return new VirginMoney2GroupHttpClientFactory(properties, meterRegistry, objectMapper);
    }

    private AccountRequestService getAccountRequestService(RestClient restClient,
                                                           AuthenticationService authenticationService) {
        return new DefaultAccountAccessConsentRequestServiceV2(authenticationService,
                restClient,
                ENDPOINT_VERSION,
                CONSENT_PERMISSIONS);
    }

    private AuthenticationService getAuthenticationService(VirginMoney2GroupProperties properties, Clock clock, TokenScope tokenScope) {
        return new VirginMoney2GroupAuthenticationServiceV1(properties.getOAuthAuthorizationUrl(),
                /**
                 * C4PO-9893 Bank informed us that, in their implementation of SCA exemption, refresh token will be issued only once
                 * during one final SCA (grant_type=authorization_code)
                 * this decorator is added to be prepared for such change
                 * TODO C4PO-11529 after 30.09.2022 verify that bank starts workings as informed and perform action in {@link OneRefreshTokenDecorator}
                 */
                new OneRefreshTokenDecorator(new VirginMoney2GroupClientSecretBasicOauth2Client(new VirginMoney2GroupTokenBodyProducer(tokenScope),
                        properties,
                        false)),
                new ExternalUserRequestTokenSigner(JWS_ALGORITHM),
                new DefaultTokenClaimsProducer(new DefaultJwtClaimsProducer(DefaultAuthMeans::getClientId,
                        properties.getAudience())),
                clock);
    }

    private FetchDataServiceV2 getFetchDataService(VirginMoney2GroupProperties properties,
                                                   RestClient restClient,
                                                   PartiesRestClient partiesRestClient,
                                                   Clock clock) {
        Supplier<List<OBBalanceType1Code>> currentBalanceTypeSupplier = () -> Collections.singletonList(INTERIMBOOKED);
        Supplier<List<OBBalanceType1Code>> availableBalanceTypeSupplier = () -> Collections.singletonList(INTERIMAVAILABLE);
        DefaultAccountReferenceTypeMapper accountReferenceTypeMapper = new DefaultAccountReferenceTypeMapper();
        DefaultTransactionStatusMapper transactionStatusMapper = new DefaultTransactionStatusMapper();
        DefaultCurrencyMapper currencyCodeMapper = new DefaultCurrencyMapper();
        DefaultBalanceMapper balanceMapper = new DefaultBalanceMapper();
        DefaultBalanceAmountMapper balanceAmountMapper = new DefaultBalanceAmountMapper(currencyCodeMapper,
                balanceMapper);
        DefaultDateTimeMapper mapDateTimeFunction = new DefaultDateTimeMapper(ZONE_ID);
        DefaultAmountParser amountParser = new DefaultAmountParser();
        DefaultSchemeMapper schemeMapper = new DefaultSchemeMapper();
        DefaultAccountIdMapper accountIdMapper = new DefaultAccountIdMapper();
        return new VirginMoney2GroupFetchDataService(restClient,
                partiesRestClient,
                properties,
                new DefaultTransactionMapper(new DefaultExtendedTransactionMapper(accountReferenceTypeMapper,
                        transactionStatusMapper,
                        balanceAmountMapper,
                        false,
                        ZONE_ID),
                        mapDateTimeFunction,
                        transactionStatusMapper,
                        amountParser,
                        new DefaultTransactionTypeMapper()),
                new DefaultDirectDebitMapper(ZONE_ID,
                        amountParser),
                new DefaultStandingOrderMapper(new DefaultPeriodMapper(),
                        amountParser,
                        schemeMapper,
                        mapDateTimeFunction),
                new DefaultPartiesMapper(),
                new DefaultAccountMapperV3(currentBalanceTypeSupplier,
                        availableBalanceTypeSupplier,
                        currencyCodeMapper,
                        accountIdMapper,
                        new DefaultAccountTypeMapper(),
                        new CreditCardMapper(),
                        new DefaultAccountNumberMapperV2(schemeMapper),
                        new DefaultAccountNameMapper(accountIdMapper),
                        balanceMapper,
                        new DefaultExtendedAccountMapper(accountReferenceTypeMapper,
                                currencyCodeMapper,
                                new DefaultExtendedBalancesMapper(balanceAmountMapper,
                                        new DefaultBalanceTypeMapper(),
                                        ZONE_ID)),
                        new DefaultSupportedSchemeAccountFilter(),
                        clock),
                new DefaultAccountFilter(),
                new DefaultSupportedAccountsSupplier(),
                Duration.ofMinutes(5),
                ENDPOINT_VERSION,
                clock);
    }

    private RestClient getRestClient(ObjectMapper objectMapper) {
        return new DefaultRestClient(new ExternalPaymentRequestSigner(objectMapper,
                JWS_ALGORITHM));
    }

    private PartiesRestClient getPartiesRestClient() {
        return new DefaultPartiesRestClient();
    }
}
