package com.yolt.providers.openbanking.ais.tsbgroup.tsb.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.openbanking.ais.common.HsmUtils;
import com.yolt.providers.openbanking.ais.generic2.GenericBasePaymentProviderV2;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.claims.producer.DefaultJwtClaimsProducer;
import com.yolt.providers.openbanking.ais.generic2.claims.producer.ExpiringJwtClaimsProducerDecorator;
import com.yolt.providers.openbanking.ais.generic2.claims.token.DefaultTokenClaimsProducer;
import com.yolt.providers.openbanking.ais.generic2.common.OpenBankingTokenScope;
import com.yolt.providers.openbanking.ais.generic2.common.ProviderIdentification;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import com.yolt.providers.openbanking.ais.generic2.http.DefaultHttpClientFactory;
import com.yolt.providers.openbanking.ais.generic2.oauth2.implementations.DefaultClientSecretPostOauth2Client;
import com.yolt.providers.openbanking.ais.generic2.service.AuthenticationService;
import com.yolt.providers.openbanking.ais.generic2.service.DefaultAuthenticationService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.accountaccessconsentrequestservice.DefaultAccountAccessConsentRequestService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.accountrequestservice.AccountRequestService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.DefaultAccountFilter;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.DefaultConsentWindow;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.FetchDataService;
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
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.scheme.DefaultSchemeMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.scheme.SchemeMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.time.DefaultDateTimeMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transaction.DefaultTransactionMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transactionstatus.DefaultTransactionStatusMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transactionstatus.PendingAsNullTransactionStatusMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transactiontype.DefaultTransactionTypeMapper;
import com.yolt.providers.openbanking.ais.generic2.service.restclient.DefaultRestClient;
import com.yolt.providers.openbanking.ais.generic2.signer.ExternalPaymentRequestSigner;
import com.yolt.providers.openbanking.ais.generic2.signer.ExternalUserRequestTokenSigner;
import com.yolt.providers.openbanking.ais.generic2.signer.UserRequestTokenSigner;
import com.yolt.providers.openbanking.ais.tsbgroup.common.TsbGroupBaseDataProvider;
import com.yolt.providers.openbanking.ais.tsbgroup.common.TsbGroupCommonPaymentProviderFactory;
import com.yolt.providers.openbanking.ais.tsbgroup.common.auth.TsbGroupAuthMeansBuilderV3;
import com.yolt.providers.openbanking.ais.tsbgroup.common.mapper.TsbGroupExtendedAmountMapper;
import com.yolt.providers.openbanking.ais.tsbgroup.common.service.TsbGroupRegistrationServiceV2;
import com.yolt.providers.openbanking.ais.tsbgroup.common.service.ais.fetchdataservice.TsbGroupFetchDataServiceV5;
import com.yolt.providers.openbanking.ais.tsbgroup.common.service.restclient.TsbGroupRegistrationRestClientV2;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBAccount6;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBBalanceType1Code;
import io.micrometer.core.instrument.MeterRegistry;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.yolt.providers.openbanking.dto.ais.openbanking316.OBBalanceType1Code.INTERIMAVAILABLE;
import static com.yolt.providers.openbanking.dto.ais.openbanking316.OBBalanceType1Code.INTERIMBOOKED;

@Configuration
public class TsbBeanConfigV2 {
    private static final String IDENTIFIER = "TSB_BANK";
    private static final String DISPLAY_NAME = "TSB";
    private static final String ENDPOINT_VERSION = "/v3.1";
    private static final ZoneId ZONE_ID = ZoneId.of("Europe/London");
    private static final String JWS_SIGNING_ALGORITHM = AlgorithmIdentifiers.RSA_PSS_USING_SHA256;
    private final UserRequestTokenSigner userRequestTokenSigner = new ExternalUserRequestTokenSigner(JWS_SIGNING_ALGORITHM);
    private final Function<Map<String, BasicAuthenticationMean>, DefaultAuthMeans> getAuthenticationMeans = typedAuthMeans -> TsbGroupAuthMeansBuilderV3.createAuthenticationMeans(typedAuthMeans, IDENTIFIER);
    private final Supplier<Map<String, TypedAuthenticationMeans>> typedAuthenticationMeansSupplier = TsbGroupAuthMeansBuilderV3.getTypedAuthenticationMeans();

    @Bean("TsbDataProviderV6")
    public TsbGroupBaseDataProvider getTsbDataProviderV6(TsbPropertiesV2 properties,
                                                         MeterRegistry registry,
                                                         Clock clock,
                                                         @Qualifier("OpenBanking") ObjectMapper objectMapper) {
        ProviderIdentification providerIdentification = new ProviderIdentification(IDENTIFIER, DISPLAY_NAME, ProviderVersion.VERSION_6);
        Supplier<Optional<KeyRequirements>> signingKeyRequirementSupplier = () -> HsmUtils.getKeyRequirements(TsbGroupAuthMeansBuilderV3.SIGNING_PRIVATE_KEY_ID_NAME);
        Supplier<Optional<KeyRequirements>> transportKeyRequirementSupplier = () -> HsmUtils.getKeyRequirements(TsbGroupAuthMeansBuilderV3.TRANSPORT_PRIVATE_KEY_ID_NAME, TsbGroupAuthMeansBuilderV3.TRANSPORT_CERTIFICATE_NAME);
        Supplier<List<OBBalanceType1Code>> currentBalanceTypeSupplier = () -> Arrays.asList(INTERIMBOOKED);
        Supplier<List<OBBalanceType1Code>> availableBalanceTypeSupplier = () -> Arrays.asList(INTERIMAVAILABLE);
        Function<OBAccount6, String> accountNameFallbackSupplier = account -> "TSB Account";
        Supplier<List<OBBalanceType1Code>> currentBalanceTypeForCreditCardSupplier = () -> Arrays.asList(INTERIMAVAILABLE);
        Supplier<List<OBBalanceType1Code>> availableBalanceTypeForCreditCardSupplier = availableBalanceTypeSupplier;
        AuthenticationService authenticationService = getAuthenticationService(properties, clock, false);
        AccountRequestService accountRequestService = getAccountRequestService(objectMapper, authenticationService);
        SchemeMapper schemeMapper = new DefaultSchemeMapper();
        Function<String, CurrencyCode> currencyMapper = new DefaultCurrencyMapper();
        BalanceMapper balanceMapper = new DefaultBalanceMapper();
        Function<String, ZonedDateTime> zonedDateTimeMapper = new DefaultDateTimeMapper(ZONE_ID);
        AccountReferenceTypeMapper accountReferenceTypeMapper = new DefaultAccountReferenceTypeMapper();
        FetchDataService fetchDataService =
                new TsbGroupFetchDataServiceV5(
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
                        new DefaultAccountMapper(
                                currentBalanceTypeSupplier,
                                availableBalanceTypeSupplier,
                                currentBalanceTypeForCreditCardSupplier,
                                availableBalanceTypeForCreditCardSupplier,
                                currencyMapper,
                                new DefaultAccountIdMapper(),
                                new DefaultAccountTypeMapper(),
                                new CreditCardMapper(),
                                new AccountNumberMapper(schemeMapper),
                                new DefaultAccountNameMapper(accountNameFallbackSupplier),
                                balanceMapper,
                                new DefaultExtendedAccountMapper(
                                        accountReferenceTypeMapper,
                                        currencyMapper,
                                        new DefaultExtendedBalancesMapper(
                                                new DefaultBalanceAmountMapper(
                                                        currencyMapper,
                                                        new TsbGroupExtendedAmountMapper()),
                                                new DefaultBalanceTypeMapper(),
                                                ZONE_ID)),
                                new DefaultSupportedSchemeAccountFilter(),
                                clock),
                        new DefaultAccountFilter(),
                        new DefaultSupportedAccountsSupplier(),
                        DefaultConsentWindow.DURATION,
                        ENDPOINT_VERSION,
                        clock);
        return new TsbGroupBaseDataProvider(
                fetchDataService,
                accountRequestService,
                authenticationService,
                getHttpClientFactory(properties, registry, objectMapper),
                TokenScope.builder()
                        .grantScope(OpenBankingTokenScope.ACCOUNTS.getGrantScope())
                        .authorizationUrlScope(OpenBankingTokenScope.ACCOUNTS.getAuthorizationUrlScope())
                        .build(),
                providerIdentification,
                getAuthenticationMeans,
                typedAuthenticationMeansSupplier,
                new DefaultAccessMeansMapper<AccessMeans>(objectMapper),
                signingKeyRequirementSupplier,
                transportKeyRequirementSupplier,
                () -> new ConsentValidityRules(Set.of("COMMON_HEADER_SECURE_TEXT")),
                getTsbGroupRegistrationService(objectMapper, properties));
    }


    @Bean("TsbPaymentProviderV5")
    public GenericBasePaymentProviderV2 getTsbPaymentProviderV5(TsbPropertiesV2 properties,
                                                                MeterRegistry registry,
                                                                Clock clock,
                                                                @Qualifier("OpenBanking") ObjectMapper objectMapper) {
        ProviderIdentification providerIdentification = new ProviderIdentification(IDENTIFIER, DISPLAY_NAME, ProviderVersion.VERSION_5);
        return TsbGroupCommonPaymentProviderFactory.createPaymentProvider(providerIdentification,
                getAuthenticationService(properties, clock, true),
                properties,
                registry,
                objectMapper,
                () -> ENDPOINT_VERSION,
                clock);
    }

    private DefaultAccountAccessConsentRequestService getAccountRequestService(ObjectMapper objectMapper,
                                                                               AuthenticationService authenticationService) {
        return new DefaultAccountAccessConsentRequestService(
                authenticationService,
                getRestClient(objectMapper),
                ENDPOINT_VERSION);
    }

    private DefaultAuthenticationService getAuthenticationService(TsbPropertiesV2 properties, Clock clock, boolean isInPisFlow) {
        return new DefaultAuthenticationService(
                properties.getOAuthAuthorizationUrl(),
                new DefaultClientSecretPostOauth2Client(properties, isInPisFlow),
                userRequestTokenSigner,
                new DefaultTokenClaimsProducer(
                        new ExpiringJwtClaimsProducerDecorator(
                                new DefaultJwtClaimsProducer(
                                        DefaultAuthMeans::getClientId,
                                        properties.getAudience()), properties.getConsentExpirationMin())),
                clock);
    }

    private DefaultRestClient getRestClient(ObjectMapper objectMapper) {
        return new DefaultRestClient(
                new ExternalPaymentRequestSigner(
                        objectMapper,
                        JWS_SIGNING_ALGORITHM));
    }

    private DefaultHttpClientFactory getHttpClientFactory(TsbPropertiesV2 properties, MeterRegistry registry, ObjectMapper objectMapper) {
        return new DefaultHttpClientFactory(properties, registry, objectMapper);
    }

    private TsbGroupRegistrationServiceV2 getTsbGroupRegistrationService(final ObjectMapper mapper,
                                                                         final TsbPropertiesV2 properties) {
        return new TsbGroupRegistrationServiceV2(new TsbGroupRegistrationRestClientV2(new ExternalPaymentRequestSigner(mapper, AlgorithmIdentifiers.RSA_PSS_USING_SHA256)), properties);
    }
}
