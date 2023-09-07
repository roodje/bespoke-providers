package com.yolt.providers.openbanking.ais.vanquisgroup.vanquis.beanconfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.openbanking.ais.common.HsmUtils;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.claims.producer.DefaultJwtClaimsProducer;
import com.yolt.providers.openbanking.ais.generic2.common.OpenBankingTokenScope;
import com.yolt.providers.openbanking.ais.generic2.common.ProviderIdentification;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import com.yolt.providers.openbanking.ais.generic2.oauth2.clientassertion.DefaultClientAssertionProducer;
import com.yolt.providers.openbanking.ais.generic2.oauth2.implementations.PrivateKeyJwtOauth2Client;
import com.yolt.providers.openbanking.ais.generic2.service.AuthenticationService;
import com.yolt.providers.openbanking.ais.generic2.service.DefaultAuthenticationService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.accountrequestservice.AccountRequestService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.DefaultAccountFilter;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.DefaultConsentWindow;
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
import com.yolt.providers.openbanking.ais.vanquisgroup.VanquisAutoOnboardingServiceV2;
import com.yolt.providers.openbanking.ais.vanquisgroup.common.VanquisGroupBaseDataProviderV2;
import com.yolt.providers.openbanking.ais.vanquisgroup.common.auth.VanquisGroupAuthMeansBuilderV2;
import com.yolt.providers.openbanking.ais.vanquisgroup.common.claims.VanquisGroupTokenClaimsProducerV2;
import com.yolt.providers.openbanking.ais.vanquisgroup.common.http.VanquisHttpClientFactory;
import com.yolt.providers.openbanking.ais.vanquisgroup.common.http.VanquisRestClient;
import com.yolt.providers.openbanking.ais.vanquisgroup.common.ouath2.VanquisAccessTokenBodyProducer;
import com.yolt.providers.openbanking.ais.vanquisgroup.common.service.ais.accountrequestservice.VanquisGroupAccountRequestServiceV2;
import com.yolt.providers.openbanking.ais.vanquisgroup.common.service.ais.fetchdataservice.VanquisGroupFetchDataServiceV3;
import com.yolt.providers.openbanking.ais.vanquisgroup.common.service.ais.mappers.VanquisExtendedBalancesMapper;
import com.yolt.providers.openbanking.ais.vanquisgroup.vanquis.VanquisPropertiesV2;
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
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.yolt.providers.common.versioning.ProviderVersion.VERSION_3;
import static com.yolt.providers.openbanking.ais.vanquisgroup.common.auth.VanquisGroupAuthMeansBuilderV2.*;
import static com.yolt.providers.openbanking.dto.ais.openbanking316.OBBalanceType1Code.FORWARDAVAILABLE;
import static com.yolt.providers.openbanking.dto.ais.openbanking316.OBBalanceType1Code.OPENINGCLEARED;

@Configuration
public class VanquisBeanConfigV2 {

    private static final String IDENTIFIER = "VANQUIS_BANK";
    private static final String DISPLAY_NAME = "Vanquis Bank (UK)";
    private static final String ENDPOINT_VERSION = "/v3.1";
    private static final ZoneId ZONE_ID = ZoneId.of("Europe/London");
    private static final String JWS_SIGNING_ALGORITHM = AlgorithmIdentifiers.RSA_PSS_USING_SHA256;
    private static final String ACCOUNT_NAME_FALLBACK_VALUE = "Vanquis Bank Account";
    private static final String AUTH_URL_SCOPE = "openid accounts offline_access";
    private final UserRequestTokenSigner userRequestTokenSigner = new ExternalUserRequestTokenSigner(JWS_SIGNING_ALGORITHM);

    @Bean("VanquisDataProviderV3")
    public VanquisGroupBaseDataProviderV2 getVanquisDataProviderV3(VanquisPropertiesV2 properties,
                                                                   MeterRegistry registry,
                                                                   Clock clock,
                                                                   @Qualifier("OpenBanking") ObjectMapper objectMapper) {
        ProviderIdentification providerIdentification = new ProviderIdentification(IDENTIFIER, DISPLAY_NAME, VERSION_3);
        Supplier<List<OBBalanceType1Code>> currentBalanceTypeSupplier = () -> List.of(OPENINGCLEARED);
        Supplier<List<OBBalanceType1Code>> availableBalanceTypeSupplier = () -> List.of(FORWARDAVAILABLE);
        Function<Map<String, BasicAuthenticationMean>, DefaultAuthMeans> authenticationMeansFactoryFunction = authMeans -> VanquisGroupAuthMeansBuilderV2.createAuthenticationMeans(authMeans, IDENTIFIER);
        Supplier<Map<String, TypedAuthenticationMeans>> typedAuthenticationMeansSupplier = () -> getTypedAuthenticationMeans();
        Function<OBAccount6, String> accountNameFallback = account -> ACCOUNT_NAME_FALLBACK_VALUE;
        AuthenticationService authenticationService = getAuthenticationService(properties, clock, false);
        AccountRequestService accountRequestService = getAccountRequestService(authenticationService);
        SchemeMapper schemeMapper = new DefaultSchemeMapper();
        Function<String, CurrencyCode> currencyMapper = new DefaultCurrencyMapper();
        BalanceMapper balanceMapper = new DefaultBalanceMapper();
        Function<String, BigDecimal> amountParser = new DefaultAmountParser();
        Function<String, ZonedDateTime> zonedDateTimeMapper = new DefaultDateTimeMapper(ZONE_ID);
        AccountReferenceTypeMapper accountReferenceTypeMapper = new DefaultAccountReferenceTypeMapper();
        Supplier<Optional<KeyRequirements>> signingKeyRequirementsSupplier = () -> HsmUtils.getKeyRequirements(SIGNING_PRIVATE_KEY_ID_NAME);
        Supplier<Optional<KeyRequirements>> transportKeyRequirementsSupplier = () -> HsmUtils.getKeyRequirements(TRANSPORT_PRIVATE_KEY_ID_NAME, TRANSPORT_CERTIFICATE_NAME);
        Supplier<ConsentValidityRules> consentValidityRules = () -> ConsentValidityRules.EMPTY_RULES_SET;
        VanquisGroupFetchDataServiceV3 fetchDataService =
                new VanquisGroupFetchDataServiceV3(
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
                                amountParser,
                                new DefaultTransactionTypeMapper()),
                        new DefaultDirectDebitMapper(ZONE_ID, amountParser),
                        new DefaultStandingOrderMapper(
                                new DefaultPeriodMapper(),
                                amountParser,
                                schemeMapper,
                                zonedDateTimeMapper),
                        new DefaultAccountMapperV2(
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
                                        new VanquisExtendedBalancesMapper(
                                                new DefaultBalanceAmountMapper(
                                                        currencyMapper,
                                                        new DefaultBalanceMapper()),
                                                new DefaultBalanceTypeMapper())),
                                new DefaultSupportedSchemeAccountFilter(),
                                clock),
                        new DefaultAccountFilter(),
                        new DefaultSupportedAccountsSupplier(),
                        DefaultConsentWindow.DURATION,
                        ENDPOINT_VERSION,
                        clock);
        return new VanquisGroupBaseDataProviderV2(
                fetchDataService,
                accountRequestService,
                authenticationService,
                getHttpClientFactory(properties, registry, objectMapper),
                TokenScope.builder()
                        .grantScope(OpenBankingTokenScope.ACCOUNTS.getGrantScope())
                        .authorizationUrlScope(AUTH_URL_SCOPE)
                        .build(),
                providerIdentification,
                authenticationMeansFactoryFunction,
                typedAuthenticationMeansSupplier,
                new DefaultAccessMeansMapper(objectMapper),
                signingKeyRequirementsSupplier,
                transportKeyRequirementsSupplier,
                consentValidityRules,
                properties,
                new VanquisAutoOnboardingServiceV2());
    }

    private VanquisGroupAccountRequestServiceV2 getAccountRequestService(AuthenticationService authenticationService) {
        return new VanquisGroupAccountRequestServiceV2(
                authenticationService,
                getRestClient(),
                ENDPOINT_VERSION);
    }

    private DefaultAuthenticationService getAuthenticationService(VanquisPropertiesV2 properties, Clock clock, boolean isInPisFlow) {
        return new DefaultAuthenticationService(
                properties.getOAuthAuthorizationUrl(),
                new PrivateKeyJwtOauth2Client<>(properties.getOAuthTokenUrl(), new VanquisAccessTokenBodyProducer(), new DefaultClientAssertionProducer(userRequestTokenSigner, properties.getOAuthTokenUrl()), isInPisFlow),
                userRequestTokenSigner,
                new VanquisGroupTokenClaimsProducerV2(
                        new DefaultJwtClaimsProducer(
                                authMeans -> authMeans.getClientId(),
                                properties.getAudience()
                        )
                ),
                clock
        );
    }

    private VanquisRestClient getRestClient() {
        return new VanquisRestClient(null);
    }

    private VanquisHttpClientFactory getHttpClientFactory(VanquisPropertiesV2 properties, MeterRegistry registry, ObjectMapper objectMapper) {
        return new VanquisHttpClientFactory(properties, registry, objectMapper);
    }

    public Map<String, TypedAuthenticationMeans> getTypedAuthenticationMeans() {
        Map<String, TypedAuthenticationMeans> typedAuthenticationMeans = new HashMap<>();
        typedAuthenticationMeans.put(INSTITUTION_ID_NAME, TypedAuthenticationMeans.INSTITUTION_ID_STRING);
        typedAuthenticationMeans.put(CLIENT_ID_NAME, TypedAuthenticationMeans.CLIENT_ID_STRING);
        typedAuthenticationMeans.put(SIGNING_KEY_HEADER_ID_NAME, TypedAuthenticationMeans.SIGNING_KEY_ID_STRING);
        typedAuthenticationMeans.put(SIGNING_PRIVATE_KEY_ID_NAME, TypedAuthenticationMeans.KEY_ID);
        typedAuthenticationMeans.put(TRANSPORT_CERTIFICATE_NAME, TypedAuthenticationMeans.CERTIFICATE_PEM);
        typedAuthenticationMeans.put(TRANSPORT_PRIVATE_KEY_ID_NAME, TypedAuthenticationMeans.KEY_ID);
        typedAuthenticationMeans.put(SOFTWARE_ID_NAME, TypedAuthenticationMeans.SOFTWARE_ID_STRING);
        typedAuthenticationMeans.put(SOFTWARE_STATEMENT_ASSERTION_NAME, TypedAuthenticationMeans.SOFTWARE_STATEMENT_ASSERTION_STRING);
        return typedAuthenticationMeans;
    }
}