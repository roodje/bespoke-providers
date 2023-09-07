package com.yolt.providers.openbanking.ais.rbsgroup.natwestcorporate.beanconfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.openbanking.ais.common.HsmUtils;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.claims.producer.DefaultJwtClaimsProducer;
import com.yolt.providers.openbanking.ais.generic2.claims.producer.ExpiringJwtClaimsProducerDecorator;
import com.yolt.providers.openbanking.ais.generic2.claims.token.DefaultTokenClaimsProducer;
import com.yolt.providers.openbanking.ais.generic2.common.OpenBankingTokenScope;
import com.yolt.providers.openbanking.ais.generic2.common.ProviderIdentification;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClientFactory;
import com.yolt.providers.openbanking.ais.generic2.oauth2.Oauth2Client;
import com.yolt.providers.openbanking.ais.generic2.oauth2.clientassertion.DefaultClientAssertionProducer;
import com.yolt.providers.openbanking.ais.generic2.oauth2.implementations.PrivateKeyJwtOauth2Client;
import com.yolt.providers.openbanking.ais.generic2.oauth2.tokenbodysupplier.PrivateKeyJwtTokenBodyProducer;
import com.yolt.providers.openbanking.ais.generic2.service.AuthenticationService;
import com.yolt.providers.openbanking.ais.generic2.service.DefaultAuthenticationService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.accountaccessconsentrequestservice.DefaultAccountAccessConsentRequestService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.accountrequestservice.AccountRequestService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.DefaultAccountFilter;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.DefaultConsentWindow;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.DefaultFetchDataService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.FetchDataService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.supportedaccounts.DefaultSupportedAccountsSupplier;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account.*;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account.schemesupport.DefaultSupportedSchemeAccountFilter;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account.schemesupport.SupportedSchemeAccountFilter;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.accountrefferencetypemapper.AccountReferenceTypeMapper;
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
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transactionstatus.PendingAsNullTransactionStatusMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transactionstatus.TransactionStatusMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transactiontype.DefaultTransactionTypeMapper;
import com.yolt.providers.openbanking.ais.generic2.service.restclient.DefaultRestClient;
import com.yolt.providers.openbanking.ais.generic2.signer.ExternalUserRequestTokenSigner;
import com.yolt.providers.openbanking.ais.generic2.signer.UserRequestTokenSigner;
import com.yolt.providers.openbanking.ais.rbsgroup.common.RbsGroupAutoonboardingProviderV2;
import com.yolt.providers.openbanking.ais.rbsgroup.common.RbsGroupDataProviderV5;
import com.yolt.providers.openbanking.ais.rbsgroup.common.http.RbsGroupHttpClientFactoryV2;
import com.yolt.providers.openbanking.ais.rbsgroup.common.service.ais.mappers.balance.RbsGroupBalanceMapper;
import com.yolt.providers.openbanking.ais.rbsgroup.common.service.autoonboarding.JwtCreator;
import com.yolt.providers.openbanking.ais.rbsgroup.common.service.autoonboarding.RbsGroupAutoonboardingServiceV2;
import com.yolt.providers.openbanking.ais.rbsgroup.common.service.restclient.RbsGroupRestClientV5;
import com.yolt.providers.openbanking.ais.rbsgroup.natwestcorporate.NatWestCorporatePropertiesV2;
import com.yolt.providers.openbanking.dto.ais.openbanking316.*;
import io.micrometer.core.instrument.MeterRegistry;
import nl.ing.lovebird.extendeddata.account.BalanceDTO;
import nl.ing.lovebird.extendeddata.account.BalanceType;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import nl.ing.lovebird.providerdomain.*;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.MultiValueMap;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import static com.yolt.providers.common.versioning.ProviderVersion.VERSION_10;
import static com.yolt.providers.openbanking.ais.rbsgroup.common.auth.RbsGroupAuthMeansBuilderV4.*;
import static com.yolt.providers.openbanking.dto.ais.openbanking316.OBBalanceType1Code.EXPECTED;
import static com.yolt.providers.openbanking.dto.ais.openbanking316.OBBalanceType1Code.FORWARDAVAILABLE;

@Configuration
public class NatWestCorporateBeanConfigV2 {

    private static final String ENDPOINT_VERSION = "/v3.1";
    private static final String IDENTIFIER = "NATWEST_CORPO";
    private static final String DISPLAY_NAME = "Natwest - corporate accounts";
    private static final Function<OBAccount6, String> ACCOUNT_NAME_FALLBACK = account -> "Natwest Corporate Account";
    private static final int EXPIRATION_TIME_IN_MINUTES = 1;

    @Bean("NatWestCorporateDataProviderV10")
    public RbsGroupDataProviderV5 getNatWestCorporateDataProviderV10(NatWestCorporatePropertiesV2 properties,
                                                                     MeterRegistry registry,
                                                                     Clock clock,
                                                                     @Qualifier("OpenBanking") ObjectMapper objectMapper) {
        String jwsSigningAlgorithm = AlgorithmIdentifiers.RSA_PSS_USING_SHA256;
        ExternalUserRequestTokenSigner tokenSigner = new ExternalUserRequestTokenSigner(jwsSigningAlgorithm);
        FetchDataService fetchDataService = getFetchDataService(properties, clock);
        AuthenticationService authenticationService = getAuthenticationService(properties, tokenSigner, clock, false);
        AccountRequestService accountRequestService = getAccountRequestService(authenticationService, ENDPOINT_VERSION);
        TokenScope scope = TokenScope.builder()
                .grantScope(OpenBankingTokenScope.ACCOUNTS.getGrantScope())
                .authorizationUrlScope(OpenBankingTokenScope.ACCOUNTS.getAuthorizationUrlScope())
                .build();
        ProviderIdentification providerIdentification = new ProviderIdentification(IDENTIFIER, DISPLAY_NAME, VERSION_10);
        HttpClientFactory httpClientFactory = new RbsGroupHttpClientFactoryV2(properties, registry, objectMapper);
        Function<Map<String, BasicAuthenticationMean>, DefaultAuthMeans> getAuthenticationMeans = defaultAuthMeans -> createAuthenticationMeansForAis(DISPLAY_NAME, defaultAuthMeans);
        Supplier<Map<String, TypedAuthenticationMeans>> typedAuthenticationMeansSupplier = getTypedAuthenticationMeans();
        AccessMeansMapper accessMeansMapper = new DefaultAccessMeansMapper(objectMapper);
        Supplier<Optional<KeyRequirements>> getSigningKeyRequirements = () -> HsmUtils.getKeyRequirements(SIGNING_PRIVATE_KEY_ID_NAME);
        Supplier<Optional<KeyRequirements>> getTransportKeyRequirements = () -> HsmUtils.getKeyRequirements(TRANSPORT_PRIVATE_KEY_ID_NAME);
        RbsGroupRestClientV5 restClient = new RbsGroupRestClientV5(null);
        JwtCreator jwtCreator = new JwtCreator();
        RbsGroupAutoonboardingServiceV2 autoonboardingService = new RbsGroupAutoonboardingServiceV2(restClient, properties, jwtCreator, authenticationService);
        RbsGroupAutoonboardingProviderV2 autoonboardingProvider = new RbsGroupAutoonboardingProviderV2(IDENTIFIER, autoonboardingService);
        return new RbsGroupDataProviderV5(
                fetchDataService,
                accountRequestService,
                authenticationService,
                httpClientFactory,
                scope,
                providerIdentification,
                getAuthenticationMeans,
                typedAuthenticationMeansSupplier,
                accessMeansMapper,
                getSigningKeyRequirements,
                getTransportKeyRequirements,
                () -> ConsentValidityRules.EMPTY_RULES_SET,
                autoonboardingProvider
        );


    }

    private FetchDataService getFetchDataService(NatWestCorporatePropertiesV2 properties, Clock clock) {
        ZoneId zoneId = ZoneId.of("Europe/London");
        AccountReferenceTypeMapper accountReferenceTypeMapper = new DefaultAccountReferenceTypeMapper();
        Function<String, CurrencyCode> currencyCodeMapper = new DefaultCurrencyMapper();
        DefaultBalanceMapper balanceMapper = new DefaultBalanceMapper();
        BalanceAmountMapper balanceAmountMapper = new DefaultBalanceAmountMapper(currencyCodeMapper, balanceMapper);
        Function<OBBalanceType1Code, BalanceType> balanceTypeMapper = new DefaultBalanceTypeMapper();
        Function<List<OBReadBalance1DataBalance>, List<BalanceDTO>> balancesMapper = new DefaultExtendedBalancesMapper(
                balanceAmountMapper,
                balanceTypeMapper,
                zoneId
        );
        DefaultExtendedAccountMapper extendedAccountMapper = new DefaultExtendedAccountMapper(
                accountReferenceTypeMapper,
                currencyCodeMapper,
                balancesMapper
        );
        DefaultBalanceMapper extendedTransactionBalanceMapper = new RbsGroupBalanceMapper();
        BalanceAmountMapper extendedTransactionBalanceAmountMapper = new DefaultBalanceAmountMapper(currencyCodeMapper, extendedTransactionBalanceMapper);
        TransactionStatusMapper transactionStatusMapper = new PendingAsNullTransactionStatusMapper();
        Function<OBTransaction6, ExtendedTransactionDTO> extendedTransactionMapper = new DefaultExtendedTransactionMapper(
                accountReferenceTypeMapper,
                transactionStatusMapper,
                extendedTransactionBalanceAmountMapper,
                true,
                zoneId
        );
        Function<String, ZonedDateTime> mapDateTimeFunction = new DefaultDateTimeMapper(zoneId);
        Function<String, BigDecimal> amountParser = new DefaultAmountParser();
        Function<OBCreditDebitCode1, ProviderTransactionType> transactionTypeMapper = new DefaultTransactionTypeMapper();

        Function<OBTransaction6, ProviderTransactionDTO> transactionMapper = new DefaultTransactionMapper(
                extendedTransactionMapper,
                mapDateTimeFunction,
                transactionStatusMapper,
                amountParser,
                transactionTypeMapper
        );
        Function<OBReadDirectDebit2DataDirectDebit, Optional<DirectDebitDTO>> directDebitMapper = new DefaultDirectDebitMapper(zoneId, amountParser);
        Function<String, Period> createPeriodFromFrequency = new DefaultPeriodMapper();
        DefaultSchemeMapper schemeMapper = new DefaultSchemeMapper();
        DefaultDateTimeMapper zonedDateTimeMapper = new DefaultDateTimeMapper(zoneId);
        Function<OBStandingOrder6, StandingOrderDTO> standingOrderMapper = new DefaultStandingOrderMapper(createPeriodFromFrequency, amountParser, schemeMapper, zonedDateTimeMapper);
        Supplier<List<OBBalanceType1Code>> getCurrentBalanceType = () -> Collections.singletonList(FORWARDAVAILABLE);
        Supplier<List<OBBalanceType1Code>> getAvailableBalanceType = () -> Collections.singletonList(EXPECTED);
        Function<OBAccount6, String> accountIdMapper = new DefaultAccountIdMapper();
        Function<OBExternalAccountSubType1Code, AccountType> accountTypeMapper = new DefaultAccountTypeMapper();
        Function<BigDecimal, ProviderCreditCardDTO> creditCardMapper = new CreditCardMapper();
        Function<OBAccount4Account, ProviderAccountNumberDTO> accountNumberMapper = new AccountNumberMapper(schemeMapper);
        AccountNameMapper accountNameMapper = new DefaultAccountNameMapper(ACCOUNT_NAME_FALLBACK);
        SupportedSchemeAccountFilter supportedSchemeAccountFilter = new DefaultSupportedSchemeAccountFilter();
        DefaultAccountMapper accountMapper = new DefaultAccountMapper(
                getCurrentBalanceType,
                getAvailableBalanceType,
                currencyCodeMapper,
                accountIdMapper,
                accountTypeMapper,
                creditCardMapper,
                accountNumberMapper,
                accountNameMapper,
                balanceMapper,
                extendedAccountMapper,
                supportedSchemeAccountFilter,
                clock
        );
        UnaryOperator<List<OBAccount6>> accountFilter = new DefaultAccountFilter();
        Supplier<Set<OBExternalAccountSubType1Code>> supportedAccountSupplier = new DefaultSupportedAccountsSupplier();
        Duration consentWindow = DefaultConsentWindow.DURATION;

        return new DefaultFetchDataService(
                new DefaultRestClient(null),
                properties,
                transactionMapper,
                directDebitMapper,
                standingOrderMapper,
                accountMapper,
                accountFilter,
                supportedAccountSupplier,
                consentWindow,
                ENDPOINT_VERSION,
                clock
        );
    }

    private AuthenticationService getAuthenticationService(NatWestCorporatePropertiesV2 properties,
                                                           ExternalUserRequestTokenSigner tokenSigner,
                                                           Clock clock,
                                                           boolean isInPisFlow) {
        UserRequestTokenSigner userRequestTokenSigner = new ExternalUserRequestTokenSigner(AlgorithmIdentifiers.RSA_PSS_USING_SHA256);
        Oauth2Client oauth2Client = new PrivateKeyJwtOauth2Client<MultiValueMap<String, String>>(
                properties.getOAuthTokenUrl(),
                new PrivateKeyJwtTokenBodyProducer(),
                new DefaultClientAssertionProducer(
                        userRequestTokenSigner,
                        properties.getOAuthTokenUrl()
                ),
                isInPisFlow);
        return new DefaultAuthenticationService(
                properties.getOAuthAuthorizationUrl(),
                oauth2Client,
                tokenSigner,
                new DefaultTokenClaimsProducer(
                        new ExpiringJwtClaimsProducerDecorator(
                                new DefaultJwtClaimsProducer(
                                        DefaultAuthMeans::getClientId,
                                        properties.getAudience()
                                ),
                                EXPIRATION_TIME_IN_MINUTES
                        )
                ),
                clock
        );
    }

    private AccountRequestService getAccountRequestService(final AuthenticationService authenticationService,
                                                           final String endpointVersion) {
        return new DefaultAccountAccessConsentRequestService(
                authenticationService,
                new RbsGroupRestClientV5(null),
                endpointVersion
        );
    }
}
