package com.yolt.providers.openbanking.ais.bankofirelandgroup.bankofireland.beanconfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.openbanking.ais.bankofirelandgroup.bankofireland.BankOfIrelandDataProvider;
import com.yolt.providers.openbanking.ais.bankofirelandgroup.bankofireland.BankOfIrelandProperties;
import com.yolt.providers.openbanking.ais.bankofirelandgroup.bankofireland.auth.BankOfIrelandAuthMeansMapper;
import com.yolt.providers.openbanking.ais.bankofirelandgroup.bankofireland.autoonboarding.BankOfIrelandAutoOnboardingService;
import com.yolt.providers.openbanking.ais.bankofirelandgroup.common.oauth2.BankOfIrelandOauthTokenBodyProducer;
import com.yolt.providers.openbanking.ais.bankofirelandgroup.common.service.ais.accountrequestservice.BankOfIrelandGroupAccountRequestServiceV5;
import com.yolt.providers.openbanking.ais.bankofirelandgroup.common.service.ais.fetchdataservice.BankOfIrelandGroupFetchDataServiceV6;
import com.yolt.providers.openbanking.ais.bankofirelandgroup.common.service.restclient.BankOfIrelandGroupRestClient;
import com.yolt.providers.openbanking.ais.common.HsmUtils;
import com.yolt.providers.openbanking.ais.generic2.DefaultProperties;
import com.yolt.providers.openbanking.ais.generic2.GenericBaseDataProvider;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.claims.producer.DefaultJwtClaimsProducer;
import com.yolt.providers.openbanking.ais.generic2.claims.producer.ExpiringJwtClaimsProducerDecorator;
import com.yolt.providers.openbanking.ais.generic2.claims.token.DefaultTokenClaimsProducer;
import com.yolt.providers.openbanking.ais.generic2.claims.token.TokenClaimsProducer;
import com.yolt.providers.openbanking.ais.generic2.common.OpenBankingTokenScope;
import com.yolt.providers.openbanking.ais.generic2.common.ProviderIdentification;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import com.yolt.providers.openbanking.ais.generic2.http.DefaultHttpClientFactory;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClientFactory;
import com.yolt.providers.openbanking.ais.generic2.oauth2.OneRefreshTokenDecorator;
import com.yolt.providers.openbanking.ais.generic2.oauth2.implementations.DefaultMutualTlsOauth2Client;
import com.yolt.providers.openbanking.ais.generic2.service.AuthenticationService;
import com.yolt.providers.openbanking.ais.generic2.service.DefaultAuthenticationService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.accountrequestservice.AccountRequestService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.DefaultAccountFilter;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.DefaultConsentWindow;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.DefaultFetchDataService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.supportedaccounts.DefaultSupportedAccountsSupplier;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account.*;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account.schemesupport.DefaultSupportedSchemeAccountFilter;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.accountrefferencetypemapper.DefaultAccountReferenceTypeMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.amount.DefaultAmountParser;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.balance.DefaultBalanceMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.balanceamount.BalanceAmountMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.balanceamount.DefaultBalanceAmountMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.balancetype.DefaultBalanceTypeMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.creditcard.CreditCardMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.currency.DefaultCurrencyMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.extendedaccount.DefaultExtendedAccountMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.extendedbalance.DefaultExtendedBalancesMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.extendedtransaction.DefaultExtendedTransactionMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.oauthtoken.AccessMeansMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.oauthtoken.DefaultAccessMeansMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.period.DefaultPeriodMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.scheme.DefaultSchemeMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.time.DefaultDateTimeMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transaction.DefaultDirectDebitMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transaction.DefaultStandingOrderMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transaction.DefaultTransactionMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transactionstatus.DefaultTransactionStatusMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transactionstatus.PendingAsNullTransactionStatusMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transactiontype.DefaultTransactionTypeMapper;
import com.yolt.providers.openbanking.ais.generic2.signer.ExternalPaymentNoB64RequestSigner;
import com.yolt.providers.openbanking.ais.generic2.signer.ExternalPaymentNoKidRequestSignerDecorator;
import com.yolt.providers.openbanking.ais.generic2.signer.ExternalUserRequestTokenSigner;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBReadDirectDebit2DataDirectDebit;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBStandingOrder6;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBTransaction6;
import io.micrometer.core.instrument.MeterRegistry;
import nl.ing.lovebird.providerdomain.DirectDebitDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import nl.ing.lovebird.providerdomain.StandingOrderDTO;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.yolt.providers.common.versioning.ProviderVersion.VERSION_7;
import static com.yolt.providers.openbanking.ais.bankofirelandgroup.bankofireland.auth.BankOfIrelandAuthMeansMapper.*;
import static com.yolt.providers.openbanking.dto.ais.openbanking316.OBBalanceType1Code.*;

@Configuration
public class BankOfIrelandDataProviderV7BeanConfig {

    private static final String ENDPOINT_VERSION = "/v3.0";
    private static final String PROVIDER_KEY = "BANK_OF_IRELAND";
    private static final String DISPLAY_NAME = "Bank of Ireland";
    private static final ZoneId ZONE_ID = ZoneId.of("Europe/London");
    private static final String ACCOUNT_NAME_FALLBACK = "Bank Of Ireland Account";
    private static final String SIGNING_ALGORITHM = AlgorithmIdentifiers.RSA_PSS_USING_SHA256;

    @Bean("BankOfIrelandDataProviderV7")
    public GenericBaseDataProvider getBankOfIrelandDataProviderV7(BankOfIrelandProperties properties,
                                                                  MeterRegistry registry,
                                                                  Clock clock,
                                                                  @Qualifier("OpenBanking") ObjectMapper objectMapper) {
        DefaultFetchDataService fetchDataService = getFetchDataService(objectMapper, clock, properties);
        AuthenticationService authenticationService = getAuthenticationService(properties, clock);
        AccountRequestService accountRequestService = getAccountRequestService(objectMapper, authenticationService);
        HttpClientFactory httpClientFactory = new DefaultHttpClientFactory(properties, registry, objectMapper);
        ProviderIdentification providerIdentification = new ProviderIdentification(PROVIDER_KEY, DISPLAY_NAME, VERSION_7);
        BankOfIrelandAuthMeansMapper authMeansMapper = new BankOfIrelandAuthMeansMapper();
        AccessMeansMapper accessMeansMapper = new DefaultAccessMeansMapper(objectMapper);
        Supplier<Optional<KeyRequirements>> signingKeyRequirements = () -> HsmUtils.getKeyRequirements(SIGNING_PRIVATE_KEY_ID_NAME_V2);
        Supplier<Optional<KeyRequirements>> transportKeyRequirements = () -> HsmUtils.getKeyRequirements(TRANSPORT_PRIVATE_KEY_ID_NAME_V2, TRANSPORT_CERTIFICATE_NAME_V2);
        Set<String> keywords = new HashSet<>();
        keywords.add("Enter your login details");
        keywords.add("User ID");
        Supplier<ConsentValidityRules> consentValidityRulesSupplier = () -> new ConsentValidityRules(keywords);
        BankOfIrelandAutoOnboardingService autoOnboardingService = getAutoOnboardingService(objectMapper, properties);

        return new BankOfIrelandDataProvider(fetchDataService, accountRequestService, authenticationService, httpClientFactory,
                getScope(), providerIdentification, authMeansMapper.getAuthMeansMapper(PROVIDER_KEY), authMeansMapper::getTypedAuthMeans,
                accessMeansMapper, signingKeyRequirements, transportKeyRequirements, consentValidityRulesSupplier, autoOnboardingService);
    }

    private BankOfIrelandAutoOnboardingService getAutoOnboardingService(ObjectMapper objectMapper,
                                                                        BankOfIrelandProperties properties) {
        return new BankOfIrelandAutoOnboardingService(getRestClient(objectMapper),
                SIGNING_ALGORITHM,
                "tls_client_auth",
                properties);
    }

    private AccountRequestService getAccountRequestService(ObjectMapper objectMapper,
                                                           AuthenticationService authenticationService) {
        return new BankOfIrelandGroupAccountRequestServiceV5(authenticationService, getRestClient(objectMapper), ENDPOINT_VERSION);
    }

    private AuthenticationService getAuthenticationService(DefaultProperties properties, Clock clock) {
        return new DefaultAuthenticationService(properties.getOAuthAuthorizationUrl(),
                /**
                 * C4PO-9877 Bank informed us that, in their implementation of SCA exemption, refresh token will be issued only once
                 * during one final SCA (grant_type=authorization_code)
                 * this decorator is added to be prepared for such change
                 * TODO C4PO-11529 after 08.10.2022 verify that bank starts workings as informed and perform action in {@link OneRefreshTokenDecorator}
                 */
                new OneRefreshTokenDecorator(new DefaultMutualTlsOauth2Client(properties, new BankOfIrelandOauthTokenBodyProducer(), false)),
                new ExternalUserRequestTokenSigner(SIGNING_ALGORITHM),
                getTokenClaimsProducer(properties),
                clock);
    }

    private DefaultFetchDataService getFetchDataService(ObjectMapper objectMapper,
                                                        Clock clock,
                                                        DefaultProperties properties) {
        return new BankOfIrelandGroupFetchDataServiceV6(getRestClient(objectMapper), properties, getTransactionMapper(), getDirectDebitMapper(),
                getStandingOrderMapper(), getAccountMapper(clock), new DefaultAccountFilter(),
                new DefaultSupportedAccountsSupplier(), DefaultConsentWindow.DURATION, ENDPOINT_VERSION, clock);
    }

    private BankOfIrelandGroupRestClient getRestClient(ObjectMapper objectMapper) {
        ExternalPaymentNoB64RequestSigner noB64RequestSigner = new ExternalPaymentNoB64RequestSigner(objectMapper, SIGNING_ALGORITHM);
        return new BankOfIrelandGroupRestClient(new ExternalPaymentNoKidRequestSignerDecorator(objectMapper, SIGNING_ALGORITHM, noB64RequestSigner));
    }

    private TokenClaimsProducer getTokenClaimsProducer(DefaultProperties properties) {
        return new DefaultTokenClaimsProducer(new ExpiringJwtClaimsProducerDecorator(
                new DefaultJwtClaimsProducer(DefaultAuthMeans::getClientId, properties.getAudience()), 60));
    }

    private Function<OBTransaction6, ProviderTransactionDTO> getTransactionMapper() {
        DefaultDateTimeMapper zonedDateTimeMapper = new DefaultDateTimeMapper(ZONE_ID);
        return new DefaultTransactionMapper(
                new DefaultExtendedTransactionMapper(
                        new DefaultAccountReferenceTypeMapper(),
                        new DefaultTransactionStatusMapper(),
                        getBalanceAmountMapper(),
                        false,
                        ZONE_ID),
                zonedDateTimeMapper,
                new PendingAsNullTransactionStatusMapper(),
                new DefaultAmountParser(),
                new DefaultTransactionTypeMapper());
    }

    private Function<OBReadDirectDebit2DataDirectDebit, Optional<DirectDebitDTO>> getDirectDebitMapper() {
        return new DefaultDirectDebitMapper(ZONE_ID, new DefaultAmountParser());
    }

    private Function<OBStandingOrder6, StandingOrderDTO> getStandingOrderMapper() {
        return new DefaultStandingOrderMapper(new DefaultPeriodMapper(),
                new DefaultAmountParser(),
                new DefaultSchemeMapper(),
                new DefaultDateTimeMapper(ZONE_ID));
    }

    private DefaultAccountMapper getAccountMapper(Clock clock) {
        return new DefaultAccountMapper(
                () -> Arrays.asList(EXPECTED),
                () -> Arrays.asList(EXPECTED),
                () -> Arrays.asList(INTERIMBOOKED),
                () -> Arrays.asList(INTERIMAVAILABLE),
                new DefaultCurrencyMapper(),
                new DefaultAccountIdMapper(),
                new DefaultAccountTypeMapper(),
                new CreditCardMapper(),
                new AccountNumberMapper(new DefaultSchemeMapper()),
                new DefaultAccountNameMapper(account -> ACCOUNT_NAME_FALLBACK),
                new DefaultBalanceMapper(),
                new DefaultExtendedAccountMapper(new DefaultAccountReferenceTypeMapper(),
                        new DefaultCurrencyMapper(),
                        new DefaultExtendedBalancesMapper(
                                getBalanceAmountMapper(),
                                new DefaultBalanceTypeMapper(),
                                ZONE_ID)),
                new DefaultSupportedSchemeAccountFilter(),
                clock);
    }

    private BalanceAmountMapper getBalanceAmountMapper() {
        return new DefaultBalanceAmountMapper(new DefaultCurrencyMapper(), new DefaultBalanceMapper());
    }

    private TokenScope getScope() {
        return TokenScope.builder()
                .grantScope(OpenBankingTokenScope.ACCOUNTS.getGrantScope())
                .authorizationUrlScope(OpenBankingTokenScope.ACCOUNTS.getAuthorizationUrlScope())
                .build();
    }
}
