package com.yolt.providers.openbanking.ais.cybgroup.yorkshire.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.exception.InvalidAuthenticationMeansException;
import com.yolt.providers.common.util.KeyUtil;
import com.yolt.providers.openbanking.ais.common.HsmEIdasUtils;
import com.yolt.providers.openbanking.ais.cybgroup.CybgGroupAutoOnboardingServiceV2;
import com.yolt.providers.openbanking.ais.cybgroup.common.CybgGroupDataProviderV3;
import com.yolt.providers.openbanking.ais.cybgroup.common.auth.CybgAuthenticationService;
import com.yolt.providers.openbanking.ais.cybgroup.common.auth.CybgGroupClientSecretBasicOauth2Client;
import com.yolt.providers.openbanking.ais.cybgroup.common.model.CybgGroupAccessMeansV2;
import com.yolt.providers.openbanking.ais.cybgroup.common.producer.CybgGroupTokenBodyProducer;
import com.yolt.providers.openbanking.ais.cybgroup.common.service.ais.CybgGroupAccountAccessConsentRequestService;
import com.yolt.providers.openbanking.ais.cybgroup.common.service.ais.CybgGroupFetchDataServiceV3;
import com.yolt.providers.openbanking.ais.generic2.DefaultProperties;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.claims.producer.DefaultJwtClaimsProducer;
import com.yolt.providers.openbanking.ais.generic2.claims.token.DefaultTokenClaimsProducer;
import com.yolt.providers.openbanking.ais.generic2.common.OpenBankingTokenScope;
import com.yolt.providers.openbanking.ais.generic2.common.ProviderIdentification;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import com.yolt.providers.openbanking.ais.generic2.http.DefaultHttpClientFactory;
import com.yolt.providers.openbanking.ais.generic2.service.AuthenticationService;
import com.yolt.providers.openbanking.ais.generic2.service.DefaultAuthenticationService;
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
import com.yolt.providers.openbanking.ais.generic2.service.restclient.DefaultRestClient;
import com.yolt.providers.openbanking.ais.generic2.signer.ExternalPaymentRequestSigner;
import com.yolt.providers.openbanking.ais.generic2.signer.ExternalUserRequestTokenSigner;
import com.yolt.providers.openbanking.ais.generic2.signer.UserRequestTokenSigner;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBAccount6;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBBalanceType1Code;
import io.micrometer.core.instrument.MeterRegistry;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Clock;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.yolt.providers.common.versioning.ProviderVersion.VERSION_3;
import static com.yolt.providers.openbanking.ais.cybgroup.common.auth.CybgGroupAuthMeansBuilderV2.*;
import static com.yolt.providers.openbanking.dto.ais.openbanking316.OBBalanceType1Code.INTERIMAVAILABLE;
import static com.yolt.providers.openbanking.dto.ais.openbanking316.OBBalanceType1Code.INTERIMBOOKED;

@Configuration
public class YorkshireBeanConfigV2 {

    private static final String IDENTIFIER = "YORKSHIRE";
    private static final String DISPLAY_NAME = "Yorkshire Bank";
    private static final String ENDPOINT_VERSION = "/v3.1";
    private static final ZoneId ZONE_ID = ZoneId.of("Europe/London");
    private static final String JWS_SIGNING_ALGORITHM = AlgorithmIdentifiers.RSA_PSS_USING_SHA256;
    private static final Duration CONSENT_WINDOW_DURATION = Duration.ofMinutes(5);
    private final UserRequestTokenSigner userRequestTokenSigner = new ExternalUserRequestTokenSigner(JWS_SIGNING_ALGORITHM);

    private static X509Certificate createCertificate(final String certificate, final String providerKey) {
        try {
            return KeyUtil.createCertificateFromPemFormat(certificate);
        } catch (CertificateException e) {
            throw new InvalidAuthenticationMeansException(providerKey, TRANSPORT_CERTIFICATE_NAME, "Cannot process certificate for thumbprint");
        }
    }

    @Bean("YorkshireDataProvider")
    public CybgGroupDataProviderV3 getYorkshireDataProvider(YorkshirePropertiesV2 properties,
                                                            MeterRegistry registry,
                                                            Clock clock,
                                                            @Qualifier("OpenBanking") ObjectMapper objectMapper) {
        ProviderIdentification providerIdentification = new ProviderIdentification(IDENTIFIER, DISPLAY_NAME, VERSION_3);
        Supplier<List<OBBalanceType1Code>> currentBalanceTypeSupplier = () -> List.of(INTERIMBOOKED);
        Supplier<List<OBBalanceType1Code>> availableBalanceTypeSupplier = () -> List.of(INTERIMAVAILABLE);
        Function<OBAccount6, String> accountNameFallback = account -> "Yorkshire Bank Account";
        SchemeMapper schemeMapper = new DefaultSchemeMapper();
        Function<String, CurrencyCode> currencyMapper = new DefaultCurrencyMapper();
        BalanceMapper balanceMapper = new DefaultBalanceMapper();
        Function<String, BigDecimal> amountParser = new DefaultAmountParser();
        Function<String, ZonedDateTime> zonedDateTimeMapper = new DefaultDateTimeMapper(ZONE_ID);
        AccountReferenceTypeMapper accountReferenceTypeMapper = new DefaultAccountReferenceTypeMapper();
        Supplier<Map<String, TypedAuthenticationMeans>> typedAuthenticationMeansSupplier = () -> getTypedAuthenticationMeans();
        Function<Map<String, BasicAuthenticationMean>, DefaultAuthMeans> authenticationMeansFactoryFunction = authMeans -> createAuthenticationMeans(authMeans, IDENTIFIER, properties);
        Supplier<Optional<KeyRequirements>> signingKeyRequirementsSupplier = () -> HsmEIdasUtils.getKeyRequirements(SIGNING_PRIVATE_KEY_ID_NAME);
        Supplier<Optional<KeyRequirements>> transportKeyRequirementsSupplier = () -> HsmEIdasUtils.getKeyRequirements(TRANSPORT_PRIVATE_KEY_ID_NAME, TRANSPORT_CERTIFICATE_NAME);
        TokenScope tokenScope = TokenScope.builder()
                .grantScope(OpenBankingTokenScope.ACCOUNTS.getGrantScope())
                .authorizationUrlScope(OpenBankingTokenScope.ACCOUNTS.getAuthorizationUrlScope())
                .build();
        CybgGroupTokenBodyProducer tokenBodyProducer = new CybgGroupTokenBodyProducer(tokenScope);
        CybgGroupFetchDataServiceV3 fetchDataService =
                new CybgGroupFetchDataServiceV3(
                        getRestClient(objectMapper),
                        properties,
                        new DefaultTransactionMapper(
                                new DefaultExtendedTransactionMapper(
                                        accountReferenceTypeMapper,
                                        new DefaultTransactionStatusMapper(),
                                        new DefaultBalanceAmountMapper(currencyMapper, balanceMapper),
                                        true,
                                        ZONE_ID),
                                zonedDateTimeMapper,
                                new PendingAsNullTransactionStatusMapper(),
                                new DefaultAmountParser(),
                                new DefaultTransactionTypeMapper()),
                        new DefaultAccountMapper(
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
                        new DefaultDirectDebitMapper(ZONE_ID, amountParser),
                        new DefaultStandingOrderMapper(new DefaultPeriodMapper(), amountParser, schemeMapper, zonedDateTimeMapper),
                        new DefaultAccountFilter(),
                        new DefaultSupportedAccountsSupplier(),
                        CONSENT_WINDOW_DURATION,
                        ENDPOINT_VERSION,
                        clock);
        AuthenticationService authenticationService = getAuthenticationService(properties, false, fetchDataService, tokenBodyProducer, clock);
        AccountRequestService accountRequestService = getAccountRequestService(objectMapper, authenticationService);
        return new CybgGroupDataProviderV3(
                properties,
                fetchDataService,
                accountRequestService,
                authenticationService,
                getHttpClientFactory(properties, registry, objectMapper),
                tokenScope,
                providerIdentification,
                authenticationMeansFactoryFunction,
                typedAuthenticationMeansSupplier,
                new DefaultAccessMeansMapper(objectMapper, CybgGroupAccessMeansV2.class),
                signingKeyRequirementsSupplier,
                transportKeyRequirementsSupplier,
                new CybgGroupAutoOnboardingServiceV2(properties),
                () -> new ConsentValidityRules(Set.of("Open Banking Web App")));
    }

    private CybgGroupAccountAccessConsentRequestService getAccountRequestService(ObjectMapper objectMapper,
                                                                                 AuthenticationService authenticationService) {
        return new CybgGroupAccountAccessConsentRequestService(
                authenticationService,
                getRestClient(objectMapper),
                ENDPOINT_VERSION);
    }

    private DefaultAuthenticationService getAuthenticationService(YorkshirePropertiesV2 properties,
                                                                  boolean isInPisFlow,
                                                                  CybgGroupFetchDataServiceV3 fetchDataService,
                                                                  CybgGroupTokenBodyProducer tokenBodyProducer,
                                                                  Clock clock) {
        return new CybgAuthenticationService(
                properties.getOAuthAuthorizationUrl(),
                new CybgGroupClientSecretBasicOauth2Client(tokenBodyProducer, properties, isInPisFlow),
                userRequestTokenSigner,
                new DefaultTokenClaimsProducer(
                        new DefaultJwtClaimsProducer(
                                DefaultAuthMeans::getClientId,
                                properties.getAudience())),
                fetchDataService,
                clock);
    }

    private DefaultRestClient getRestClient(ObjectMapper objectMapper) {
        return new DefaultRestClient(
                new ExternalPaymentRequestSigner(
                        objectMapper,
                        JWS_SIGNING_ALGORITHM));
    }

    private DefaultHttpClientFactory getHttpClientFactory(DefaultProperties properties, MeterRegistry registry, ObjectMapper objectMapper) {
        return new DefaultHttpClientFactory(properties, registry, objectMapper);
    }

    private Map<String, TypedAuthenticationMeans> getTypedAuthenticationMeans() {
        Map<String, TypedAuthenticationMeans> typedAuthenticationMeans = new HashMap<>();
        typedAuthenticationMeans.put(INSTITUTION_ID_NAME, TypedAuthenticationMeans.INSTITUTION_ID_STRING);
        typedAuthenticationMeans.put(CLIENT_ID_NAME, TypedAuthenticationMeans.CLIENT_ID_STRING);
        typedAuthenticationMeans.put(CLIENT_SECRET_NAME, TypedAuthenticationMeans.CLIENT_SECRET_STRING);
        typedAuthenticationMeans.put(SIGNING_KEY_HEADER_ID_NAME, TypedAuthenticationMeans.SIGNING_KEY_ID_STRING);
        typedAuthenticationMeans.put(TRANSPORT_CERTIFICATE_NAME, TypedAuthenticationMeans.CLIENT_TRANSPORT_CERTIFICATE_PEM);
        typedAuthenticationMeans.put(SOFTWARE_ID_NAME, TypedAuthenticationMeans.SOFTWARE_ID_STRING);
        typedAuthenticationMeans.put(SIGNING_PRIVATE_KEY_ID_NAME, TypedAuthenticationMeans.KEY_ID);
        typedAuthenticationMeans.put(TRANSPORT_PRIVATE_KEY_ID_NAME, TypedAuthenticationMeans.KEY_ID);
        typedAuthenticationMeans.put(SOFTWARE_STATEMENT_ASSERTION_NAME, TypedAuthenticationMeans.SOFTWARE_STATEMENT_ASSERTION_STRING);

        return typedAuthenticationMeans;
    }

    private DefaultAuthMeans createAuthenticationMeans(Map<String, BasicAuthenticationMean> authMeans, String providerKey, YorkshirePropertiesV2 properties) {
        return DefaultAuthMeans.builder()
                .institutionId(properties.getInstitutionId())
                .clientId(authMeans.get(CLIENT_ID_NAME).getValue())
                .clientSecret(authMeans.get(CLIENT_SECRET_NAME).getValue())
                .softwareId(authMeans.get(SOFTWARE_ID_NAME).getValue())
                .signingKeyIdHeader(authMeans.get(SIGNING_KEY_HEADER_ID_NAME).getValue())
                .transportCertificate(createCertificate(authMeans.get(TRANSPORT_CERTIFICATE_NAME).getValue(), providerKey))
                .transportPrivateKeyId(UUID.fromString(authMeans.get(TRANSPORT_PRIVATE_KEY_ID_NAME).getValue()))
                .signingPrivateKeyId(UUID.fromString(authMeans.get(SIGNING_PRIVATE_KEY_ID_NAME).getValue()))
                .build();
    }
}
