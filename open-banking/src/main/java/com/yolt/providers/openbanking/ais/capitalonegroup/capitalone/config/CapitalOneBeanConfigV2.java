package com.yolt.providers.openbanking.ais.capitalonegroup.capitalone.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.openbanking.ais.capitalonegroup.capitalone.service.CapitalOneAutoOnboardingServiceV3;
import com.yolt.providers.openbanking.ais.capitalonegroup.common.CapitalOneGroupDataProviderV3;
import com.yolt.providers.openbanking.ais.capitalonegroup.common.auth.CapitalOneAuthMeansBuilderV3;
import com.yolt.providers.openbanking.ais.capitalonegroup.common.http.CapitalOneGroupHttpClientFactoryV2;
import com.yolt.providers.openbanking.ais.capitalonegroup.common.oauth2.CapitalOneGroupPrivateKeyJwtOauth2Client;
import com.yolt.providers.openbanking.ais.capitalonegroup.common.oauth2.CapitalOneUserRequestTokenSignerV2;
import com.yolt.providers.openbanking.ais.capitalonegroup.common.service.ais.accountrequest.CapitalOneGroupAccountRequestServiceV2;
import com.yolt.providers.openbanking.ais.capitalonegroup.common.service.ais.fetchdata.CapitalOneGroupFetchDataServiceV3;
import com.yolt.providers.openbanking.ais.capitalonegroup.common.service.ais.restclient.CapitalOneGroupRestClientV2;
import com.yolt.providers.openbanking.ais.common.HsmUtils;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.claims.producer.ExtendedJwtClaimProducer;
import com.yolt.providers.openbanking.ais.generic2.claims.token.DefaultTokenClaimsProducer;
import com.yolt.providers.openbanking.ais.generic2.common.OpenBankingTokenScope;
import com.yolt.providers.openbanking.ais.generic2.common.ProviderIdentification;
import com.yolt.providers.openbanking.ais.generic2.consentvalidity.DefaultConsentValidityRulesSupplier;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import com.yolt.providers.openbanking.ais.generic2.http.DefaultHttpClientFactory;
import com.yolt.providers.openbanking.ais.generic2.oauth2.OneRefreshTokenDecorator;
import com.yolt.providers.openbanking.ais.generic2.oauth2.clientassertion.DefaultClientAssertionProducer;
import com.yolt.providers.openbanking.ais.generic2.oauth2.tokenbodysupplier.PrivateKeyJwtTokenBodyProducerWithoutScope;
import com.yolt.providers.openbanking.ais.generic2.service.AuthenticationService;
import com.yolt.providers.openbanking.ais.generic2.service.DefaultAuthenticationService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.accountaccessconsentrequestservice.DefaultAccountAccessConsentRequestService;
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
import com.yolt.providers.openbanking.ais.generic2.signer.ExternalPaymentRequestSigner;
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
import java.util.function.Function;
import java.util.function.Supplier;

import static com.yolt.providers.common.versioning.ProviderVersion.VERSION_4;
import static com.yolt.providers.openbanking.ais.capitalonegroup.common.CapitalOneGroupDataProviderV3.REGISTRATION_ACCESS_TOKEN_STRING;
import static com.yolt.providers.openbanking.ais.capitalonegroup.common.auth.CapitalOneAuthMeansBuilderV3.*;
import static com.yolt.providers.openbanking.dto.ais.openbanking316.OBBalanceType1Code.OPENINGBOOKED;

@Configuration
public class CapitalOneBeanConfigV2 {
    private static final String IDENTIFIER = "CAPITAL_ONE";
    private static final String DISPLAY_NAME = "Capital One";
    private static final String ENDPOINT_VERSION = "/v3.1";
    private static final ZoneId ZONE_ID = ZoneId.of("Europe/London");
    private static final String JWS_SIGNING_ALGORITHM = AlgorithmIdentifiers.RSA_PSS_USING_SHA256;
    private final Function<Map<String, BasicAuthenticationMean>, DefaultAuthMeans> authenticationMeansFactoryFunction =
            authMeans -> CapitalOneAuthMeansBuilderV3.createAuthenticationMeans(authMeans, IDENTIFIER);

    private final Supplier<Map<String, TypedAuthenticationMeans>> typedAuthenticationMeansSupplier = () -> {
        Map<String, TypedAuthenticationMeans> typedAuthenticationMeans = new HashMap<>();

        typedAuthenticationMeans.put(INSTITUTION_ID_NAME, TypedAuthenticationMeans.INSTITUTION_ID_STRING);
        typedAuthenticationMeans.put(CLIENT_ID_NAME, TypedAuthenticationMeans.CLIENT_ID_STRING);
        typedAuthenticationMeans.put(SIGNING_KEY_HEADER_ID_NAME, TypedAuthenticationMeans.SIGNING_KEY_ID_STRING);
        typedAuthenticationMeans.put(TRANSPORT_CERTIFICATE_NAME, TypedAuthenticationMeans.CLIENT_TRANSPORT_CERTIFICATE_PEM);
        typedAuthenticationMeans.put(SOFTWARE_ID_NAME, TypedAuthenticationMeans.SOFTWARE_ID_STRING);
        typedAuthenticationMeans.put(SIGNING_PRIVATE_KEY_ID_NAME, TypedAuthenticationMeans.KEY_ID);
        typedAuthenticationMeans.put(TRANSPORT_PRIVATE_KEY_ID_NAME, TypedAuthenticationMeans.KEY_ID);
        typedAuthenticationMeans.put(SOFTWARE_STATEMENT_ASSERTION_NAME, TypedAuthenticationMeans.SOFTWARE_STATEMENT_ASSERTION_STRING);
        typedAuthenticationMeans.put(REGISTRATION_ACCESS_TOKEN_NAME, REGISTRATION_ACCESS_TOKEN_STRING);

        return typedAuthenticationMeans;
    };

    private DefaultAccountAccessConsentRequestService getAccountRequestService(ObjectMapper objectMapper,
                                                                               AuthenticationService authenticationService) {
        return new CapitalOneGroupAccountRequestServiceV2(
                authenticationService,
                getRestClient(objectMapper),
                ENDPOINT_VERSION);
    }

    private DefaultAuthenticationService getAuthenticationService(CapitalOnePropertiesV2 properties, Clock clock, boolean isInPisFlow) {
        CapitalOneUserRequestTokenSignerV2 userRequestTokenSigner = new CapitalOneUserRequestTokenSignerV2(JWS_SIGNING_ALGORITHM);
        return new DefaultAuthenticationService(
                properties.getOAuthAuthorizationUrl(),
                //C4PO-9879 As a TPP integrating with Capital One UK, you should ensure that you are always capturing and storing the refresh_token from each request, rather than only capturing the initial refresh_token on a first time request for a given customer.
                //This change will be released at 31.09.2022
                //TODO C4PO-11529 remove usage of decorator if bank starts working as it says
                new OneRefreshTokenDecorator(
                        new CapitalOneGroupPrivateKeyJwtOauth2Client(
                                properties.getOAuthTokenUrl(),
                                new PrivateKeyJwtTokenBodyProducerWithoutScope(),
                                new DefaultClientAssertionProducer(
                                        userRequestTokenSigner,
                                        properties.getAudience()),
                                isInPisFlow)),
                userRequestTokenSigner,
                new DefaultTokenClaimsProducer(
                        new ExtendedJwtClaimProducer(
                                DefaultAuthMeans::getClientId,
                                properties.getAudience())),
                clock);
    }

    private CapitalOneGroupRestClientV2 getRestClient(ObjectMapper objectMapper) {
        return new CapitalOneGroupRestClientV2(
                new ExternalPaymentRequestSigner(
                        objectMapper,
                        JWS_SIGNING_ALGORITHM));
    }

    private DefaultHttpClientFactory getHttpClientFactory(CapitalOnePropertiesV2 properties, MeterRegistry registry, ObjectMapper objectMapper) {
        return new CapitalOneGroupHttpClientFactoryV2(properties, registry, objectMapper);
    }

    @Bean("CapitalOneDataProviderV4")
    public CapitalOneGroupDataProviderV3 getCapitalOneDataProviderV4(CapitalOnePropertiesV2 properties,
                                                                     MeterRegistry registry,
                                                                     Clock clock,
                                                                     @Qualifier("OpenBanking") ObjectMapper objectMapper) {
        ProviderIdentification providerIdentification = new ProviderIdentification(IDENTIFIER, DISPLAY_NAME, VERSION_4);
        Supplier<List<OBBalanceType1Code>> currentBalanceTypeSupplier = () -> List.of(OPENINGBOOKED);
        Supplier<List<OBBalanceType1Code>> availableBalanceTypeSupplier = () -> List.of(OPENINGBOOKED);
        Function<OBAccount6, String> accountNameFallback = account -> "Capital One Account";
        Supplier<List<OBBalanceType1Code>> getCurrentBalanceTypeForCreditCard = currentBalanceTypeSupplier;
        Supplier<List<OBBalanceType1Code>> getAvailableBalanceTypeForCreditCard = availableBalanceTypeSupplier;
        AuthenticationService authenticationService = getAuthenticationService(properties, clock, false);
        AccountRequestService accountRequestService = getAccountRequestService(objectMapper, authenticationService);
        SchemeMapper schemeMapper = new DefaultSchemeMapper();
        Function<String, CurrencyCode> currencyMapper = new DefaultCurrencyMapper();
        BalanceMapper balanceMapper = new DefaultBalanceMapper();
        Function<String, BigDecimal> amountParser = new DefaultAmountParser();
        Function<String, ZonedDateTime> zonedDateTimeMapper = new DefaultDateTimeMapper(ZONE_ID);
        AccountReferenceTypeMapper accountReferenceTypeMapper = new DefaultAccountReferenceTypeMapper();
        CapitalOneGroupFetchDataServiceV3 fetchDataService =
                new CapitalOneGroupFetchDataServiceV3(
                        getRestClient(objectMapper),
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
                                getCurrentBalanceTypeForCreditCard,
                                getAvailableBalanceTypeForCreditCard,
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
                        DefaultConsentWindow.DURATION,
                        ENDPOINT_VERSION,
                        clock);
        return new CapitalOneGroupDataProviderV3(
                fetchDataService,
                accountRequestService,
                authenticationService,
                getHttpClientFactory(properties, registry, objectMapper),
                TokenScope.builder()
                        .grantScope(OpenBankingTokenScope.ACCOUNTS.getGrantScope())
                        .authorizationUrlScope(OpenBankingTokenScope.ACCOUNTS.getAuthorizationUrlScope())
                        .build(),
                providerIdentification,
                authenticationMeansFactoryFunction,
                typedAuthenticationMeansSupplier,
                new DefaultAccessMeansMapper<>(objectMapper),
                () -> HsmUtils.getKeyRequirements(SIGNING_PRIVATE_KEY_ID_NAME),
                () -> HsmUtils.getKeyRequirements(TRANSPORT_PRIVATE_KEY_ID_NAME, TRANSPORT_CERTIFICATE_NAME),
                new DefaultConsentValidityRulesSupplier(),
                new CapitalOneAutoOnboardingServiceV3(getRestClient(objectMapper), properties));
    }
}
