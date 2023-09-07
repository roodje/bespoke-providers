package com.yolt.providers.openbanking.ais.newdaygroup.debenhams.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.openbanking.ais.common.HsmUtils;
import com.yolt.providers.openbanking.ais.generic2.DefaultProperties;
import com.yolt.providers.openbanking.ais.generic2.GenericBaseDataProvider;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.claims.producer.DefaultJwtClaimsProducer;
import com.yolt.providers.openbanking.ais.generic2.claims.token.DefaultTokenClaimsProducer;
import com.yolt.providers.openbanking.ais.generic2.common.OpenBankingTokenScope;
import com.yolt.providers.openbanking.ais.generic2.common.ProviderIdentification;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import com.yolt.providers.openbanking.ais.generic2.http.DefaultHttpClientFactory;
import com.yolt.providers.openbanking.ais.generic2.service.AuthenticationService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.accountrequestservice.AccountRequestService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.DefaultAccountFilter;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.supportedaccounts.DefaultSupportedAccountsSupplier;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account.*;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account.schemesupport.DefaultSupportedSchemeAccountFilter;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.accountrefferencetypemapper.AccountReferenceTypeMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.accountrefferencetypemapper.DefaultAccountReferenceTypeMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.amount.DefaultAmountParser;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.balance.BalanceMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.balance.DefaultBalanceMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.balanceamount.DefaultBalanceAmountMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.balancetype.DefaultBalanceTypeMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.creditcard.CreditCardMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.currency.DefaultCurrencyMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.extendedaccount.DefaultExtendedAccountMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.extendedbalance.DefaultExtendedBalancesMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.extendedtransaction.DefaultExtendedTransactionMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.oauthtoken.DefaultAccessMeansMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.period.DefaultPeriodMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.scheme.DefaultSchemeMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.scheme.SchemeMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.time.DefaultDateTimeMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transaction.DefaultDirectDebitMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transaction.DefaultStandingOrderMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transaction.DefaultTransactionMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transactionstatus.DefaultTransactionStatusMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transactionstatus.PendingAsNullTransactionStatusMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transactiontype.DefaultTransactionTypeMapper;
import com.yolt.providers.openbanking.ais.generic2.signer.ExternalUserRequestTokenSigner;
import com.yolt.providers.openbanking.ais.generic2.signer.UserRequestTokenSigner;
import com.yolt.providers.openbanking.ais.newdaygroup.common.auth.NewDayGroupAuthMeansBuilderV2;
import com.yolt.providers.openbanking.ais.newdaygroup.common.http.NewDayGroupRestClientV2;
import com.yolt.providers.openbanking.ais.newdaygroup.common.mapper.NewDayBalanceMapper;
import com.yolt.providers.openbanking.ais.newdaygroup.common.oauth2.NewDayGroupOAuth2ClientV2;
import com.yolt.providers.openbanking.ais.newdaygroup.common.service.NewDayGroupAccountAccessConsentRequestServiceV2;
import com.yolt.providers.openbanking.ais.newdaygroup.common.service.NewDayGroupAuthenticationServiceV2;
import com.yolt.providers.openbanking.ais.newdaygroup.common.service.NewDayGroupFetchDataServiceV2;
import com.yolt.providers.openbanking.ais.newdaygroup.debenhams.DebenhamsPropertiesV2;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBAccount6;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBBalanceType1Code;
import io.micrometer.core.instrument.MeterRegistry;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.yolt.providers.common.versioning.ProviderVersion.VERSION_3;
import static com.yolt.providers.openbanking.ais.newdaygroup.common.auth.NewDayGroupAuthMeansBuilderV2.*;
import static com.yolt.providers.openbanking.dto.ais.openbanking316.OBBalanceType1Code.CLOSINGBOOKED;

@Configuration
public class DebenhamsBeanConfigV2 {

    private static final String IDENTIFIER = "DEBENHAMS";
    private static final String DISPLAY_NAME = "Debenhams Credit Card";
    private static final String ENDPOINT_VERSION = "/v3.1";
    private static final ZoneId ZONE_ID = ZoneId.of("Europe/London");
    private static final String JWS_SIGNING_ALGORITHM = AlgorithmIdentifiers.RSA_PSS_USING_SHA256;
    private static final Duration INITIAL_CONSENT_WINDOW = Duration.ofMinutes(5);
    private static final String AUTH_URL_SCOPE = "openid accounts offline_access";
    private final UserRequestTokenSigner userRequestTokenSigner = new ExternalUserRequestTokenSigner(JWS_SIGNING_ALGORITHM);

    @Bean("DebenhamsDataProviderV3")
    public GenericBaseDataProvider getDebenhamsDataProviderV3(DebenhamsPropertiesV2 properties,
                                                              MeterRegistry registry,
                                                              Clock clock,
                                                              @Qualifier("OpenBanking") ObjectMapper objectMapper) {
        ProviderIdentification providerIdentification = new ProviderIdentification(IDENTIFIER, DISPLAY_NAME, VERSION_3);
        Supplier<List<OBBalanceType1Code>> currentBalanceTypeSupplier = () -> List.of(CLOSINGBOOKED);
        Function<Map<String, BasicAuthenticationMean>, DefaultAuthMeans> authenticationMeansFactoryFunction = authMeans -> NewDayGroupAuthMeansBuilderV2.createAuthenticationMeans(authMeans, IDENTIFIER);
        Supplier<Map<String, TypedAuthenticationMeans>> typedAuthenticationMeansSupplier = () -> getTypedAuthenticationMeans();
        Supplier<List<OBBalanceType1Code>> availableBalanceTypeSupplier = () -> List.of(CLOSINGBOOKED);
        Function<OBAccount6, String> accountNameFallback = account -> "Debenhams Credit Card Account";
        AuthenticationService authenticationService = getAuthenticationService(properties, clock, false);
        AccountRequestService accountRequestService = getAccountRequestService(authenticationService);
        SchemeMapper schemeMapper = new DefaultSchemeMapper();
        Function<String, CurrencyCode> currencyMapper = new DefaultCurrencyMapper();
        BalanceMapper balanceMapper = new NewDayBalanceMapper();
        Function<String, BigDecimal> amountParser = new DefaultAmountParser();
        Function<String, ZonedDateTime> zonedDateTimeMapper = new DefaultDateTimeMapper(ZONE_ID);
        AccountReferenceTypeMapper accountReferenceTypeMapper = new DefaultAccountReferenceTypeMapper();
        Supplier<Optional<KeyRequirements>> signingKeyRequirementsSupplier = () -> HsmUtils.getKeyRequirements(SIGNING_KEY_ID);
        Supplier<Optional<KeyRequirements>> transportKeyRequirementsSupplier = () -> HsmUtils.getKeyRequirements(TRANSPORT_KEY_ID, TRANSPORT_CERTIFICATE_NAME);
        Supplier<ConsentValidityRules> consentValidityRules = () -> createConsentValidityRules();
        NewDayGroupFetchDataServiceV2 fetchDataService =
                new NewDayGroupFetchDataServiceV2(
                        getRestClient(),
                        properties,
                        new DefaultTransactionMapper(
                                new DefaultExtendedTransactionMapper(
                                        accountReferenceTypeMapper,
                                        new DefaultTransactionStatusMapper(),
                                        new DefaultBalanceAmountMapper(currencyMapper, balanceMapper),
                                        false,
                                        ZONE_ID),
                                zonedDateTimeMapper,
                                new PendingAsNullTransactionStatusMapper(),
                                new DefaultAmountParser(),
                                new DefaultTransactionTypeMapper()),
                        new DefaultDirectDebitMapper(ZONE_ID, amountParser),
                        new DefaultStandingOrderMapper(new DefaultPeriodMapper(), amountParser, schemeMapper, zonedDateTimeMapper),
                        new DefaultAccountMapper(
                                currentBalanceTypeSupplier,
                                availableBalanceTypeSupplier,
                                currentBalanceTypeSupplier,
                                availableBalanceTypeSupplier,
                                currencyMapper,
                                new DefaultAccountIdMapper(),
                                new DefaultAccountTypeMapper(),
                                new CreditCardMapper(),
                                new AccountNumberMapper(schemeMapper),
                                new DefaultAccountNameMapper(accountNameFallback),
                                balanceMapper,
                                new DefaultExtendedAccountMapper(
                                        accountReferenceTypeMapper,
                                        currencyMapper,
                                        new DefaultExtendedBalancesMapper(
                                                new DefaultBalanceAmountMapper(
                                                        currencyMapper,
                                                        new DefaultBalanceMapper()),
                                                new DefaultBalanceTypeMapper(),
                                                ZONE_ID)),
                                new DefaultSupportedSchemeAccountFilter(),
                                clock),
                        new DefaultAccountFilter(),
                        new DefaultSupportedAccountsSupplier(),
                        INITIAL_CONSENT_WINDOW,
                        ENDPOINT_VERSION,
                        clock);
        return new GenericBaseDataProvider(
                fetchDataService,
                accountRequestService,
                authenticationService,
                getHttpClientFactory(properties, registry, objectMapper),
                TokenScope.builder().
                        grantScope(OpenBankingTokenScope.ACCOUNTS.getGrantScope())
                        .authorizationUrlScope(AUTH_URL_SCOPE)
                        .build(),
                providerIdentification,
                authenticationMeansFactoryFunction,
                typedAuthenticationMeansSupplier,
                new DefaultAccessMeansMapper(objectMapper),
                signingKeyRequirementsSupplier,
                transportKeyRequirementsSupplier,
                consentValidityRules);
    }

    private NewDayGroupAccountAccessConsentRequestServiceV2 getAccountRequestService(AuthenticationService authenticationService) {
        return new NewDayGroupAccountAccessConsentRequestServiceV2(
                authenticationService,
                getRestClient(),
                ENDPOINT_VERSION);
    }

    private NewDayGroupAuthenticationServiceV2 getAuthenticationService(DebenhamsPropertiesV2 properties, Clock clock, boolean isInPisFlow) {
        return new NewDayGroupAuthenticationServiceV2(
                properties.getOAuthAuthorizationUrl(),
                new NewDayGroupOAuth2ClientV2(properties, isInPisFlow),
                userRequestTokenSigner,
                new DefaultTokenClaimsProducer(
                        new DefaultJwtClaimsProducer(
                                authMeans -> authMeans.getSoftwareId(),
                                properties.getBaseUrl()
                        )
                ),
                clock
        );
    }

    private NewDayGroupRestClientV2 getRestClient() {
        return new NewDayGroupRestClientV2(null);
    }

    private DefaultHttpClientFactory getHttpClientFactory(DefaultProperties properties, MeterRegistry registry, ObjectMapper objectMapper) {
        return new DefaultHttpClientFactory(properties, registry, objectMapper);
    }

    private ConsentValidityRules createConsentValidityRules() {

        return new ConsentValidityRules(
                Set.of("Please enter your security credentials to log in. These will not be shared with third parties.",
                        "Username",
                        "Continue"));
    }

    public Map<String, TypedAuthenticationMeans> getTypedAuthenticationMeans() {
        Map<String, TypedAuthenticationMeans> typedAuthenticationMeans = new HashMap<>();
        typedAuthenticationMeans.put(INSTITUTION_ID_NAME, TypedAuthenticationMeans.INSTITUTION_ID_STRING);
        typedAuthenticationMeans.put(CLIENT_ID_NAME, TypedAuthenticationMeans.CLIENT_ID_STRING);
        typedAuthenticationMeans.put(CLIENT_SECRET_NAME, TypedAuthenticationMeans.CLIENT_SECRET_STRING);
        typedAuthenticationMeans.put(SIGNING_KEY_ID, TypedAuthenticationMeans.KEY_ID);
        typedAuthenticationMeans.put(SIGNING_KEY_HEADER_ID_NAME, TypedAuthenticationMeans.SIGNING_KEY_ID_STRING);
        typedAuthenticationMeans.put(TRANSPORT_KEY_ID, TypedAuthenticationMeans.KEY_ID);
        typedAuthenticationMeans.put(TRANSPORT_CERTIFICATE_NAME, TypedAuthenticationMeans.CLIENT_TRANSPORT_CERTIFICATE_PEM);
        typedAuthenticationMeans.put(ORGANIZATION_ID_NAME, TypedAuthenticationMeans.ORGANIZATION_ID_STRING);
        typedAuthenticationMeans.put(SOFTWARE_ID_NAME, TypedAuthenticationMeans.SOFTWARE_ID_STRING);
        typedAuthenticationMeans.put(SOFTWARE_STATEMENT_ASSERTION_NAME, TypedAuthenticationMeans.SOFTWARE_STATEMENT_ASSERTION_STRING);
        return typedAuthenticationMeans;
    }
}
