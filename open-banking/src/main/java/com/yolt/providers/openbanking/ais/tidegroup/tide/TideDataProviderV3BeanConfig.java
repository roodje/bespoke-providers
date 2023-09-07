package com.yolt.providers.openbanking.ais.tidegroup.tide;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.openbanking.ais.common.HsmUtils;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.claims.token.DefaultTokenClaimsProducer;
import com.yolt.providers.openbanking.ais.generic2.claims.token.TokenClaimsProducer;
import com.yolt.providers.openbanking.ais.generic2.common.OpenBankingTokenScope;
import com.yolt.providers.openbanking.ais.generic2.common.ProviderIdentification;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import com.yolt.providers.openbanking.ais.generic2.http.DefaultHttpClientFactory;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClientFactory;
import com.yolt.providers.openbanking.ais.generic2.oauth2.clientassertion.DefaultClientAssertionProducer;
import com.yolt.providers.openbanking.ais.generic2.oauth2.tokenbodysupplier.PrivateKeyJwtTokenBodyProducer;
import com.yolt.providers.openbanking.ais.generic2.service.AuthenticationService;
import com.yolt.providers.openbanking.ais.generic2.service.DefaultAuthenticationService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.accountaccessconsentrequestservice.DefaultAccountAccessConsentRequestService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.accountrequestservice.AccountRequestService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.DefaultAccountFilter;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.DefaultFetchDataService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account.AccountNumberMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account.DefaultAccountIdMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account.DefaultAccountMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account.DefaultAccountNameMapper;
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
import com.yolt.providers.openbanking.ais.tidegroup.common.TideGroupDataProviderV2;
import com.yolt.providers.openbanking.ais.tidegroup.common.auth.TideGroupAuthMeansMapperV3;
import com.yolt.providers.openbanking.ais.tidegroup.common.claims.TideGroupJwtClaimsProducerV1;
import com.yolt.providers.openbanking.ais.tidegroup.common.oauth2.TideGroupPrivateKeyJwtOauth2ClientV2;
import com.yolt.providers.openbanking.ais.tidegroup.common.service.TideGroupAutoOnboardingServiceV2;
import com.yolt.providers.openbanking.ais.tidegroup.common.service.TideGroupRestClientV2;
import com.yolt.providers.openbanking.ais.tidegroup.common.service.ais.TideAccountTypeMapper;
import com.yolt.providers.openbanking.ais.tidegroup.common.service.ais.TideSupportedAccountsSupplier;
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
import java.time.Duration;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.yolt.providers.common.versioning.ProviderVersion.VERSION_3;
import static com.yolt.providers.openbanking.ais.santander.auth.SantanderAuthMeansMapper.*;
import static com.yolt.providers.openbanking.dto.ais.openbanking316.OBBalanceType1Code.EXPECTED;
import static com.yolt.providers.openbanking.dto.ais.openbanking316.OBBalanceType1Code.INTERIMAVAILABLE;

@Configuration
class TideDataProviderV3BeanConfig {

    public static final Duration CONSENT_WINDOW = Duration.ofSeconds(600);
    private static final String ENDPOINT_VERSION = "/v3.1";
    private static final String PROVIDER_KEY = "TIDE";
    private static final String DISPLAY_NAME = "TIDE";
    private static final ZoneId ZONE_ID = ZoneId.of("Europe/London");
    private static final String SIGNING_ALGORITHM = AlgorithmIdentifiers.RSA_PSS_USING_SHA256;

    @Bean("TideDataProviderV3")
    public TideGroupDataProviderV2 getDataProvider(@Qualifier("OpenBanking") ObjectMapper objectMapper,
                                                   TidePropertiesV2 properties,
                                                   MeterRegistry meterRegistry,
                                                   Clock clock) {
        DefaultFetchDataService fetchDataService = getFetchDataService(objectMapper, clock, properties);
        AuthenticationService authenticationService = getAuthenticationService(properties, clock);
        AccountRequestService accountRequestService = getAccountRequestService(objectMapper, authenticationService);
        HttpClientFactory httpClientFactory = new DefaultHttpClientFactory(properties, meterRegistry, objectMapper);
        ProviderIdentification providerIdentification = new ProviderIdentification(PROVIDER_KEY, DISPLAY_NAME, VERSION_3);
        TideGroupAuthMeansMapperV3 authMeansMapper = new TideGroupAuthMeansMapperV3();
        AccessMeansMapper accessMeansMapper = new DefaultAccessMeansMapper(objectMapper);
        Supplier<Optional<KeyRequirements>> signingKeyRequirements = () -> HsmUtils.getKeyRequirements(SIGNING_PRIVATE_KEY_ID_NAME);
        Supplier<Optional<KeyRequirements>> transportKeyRequirements = () -> HsmUtils.getKeyRequirements(TRANSPORT_PRIVATE_KEY_ID_NAME, TRANSPORT_CERTIFICATE_NAME);
        Supplier<ConsentValidityRules> consentValidityRulesSupplier = () -> new ConsentValidityRules(Collections.emptySet());
        TideGroupAutoOnboardingServiceV2 autoOnboardingService = new TideGroupAutoOnboardingServiceV2(getRestClient(objectMapper), properties);

        return new TideGroupDataProviderV2(fetchDataService, accountRequestService, authenticationService, httpClientFactory,
                getScope(), providerIdentification, authMeansMapper.getAuthMeansMapper(PROVIDER_KEY), authMeansMapper::getTypedAuthMeans,
                accessMeansMapper, signingKeyRequirements, transportKeyRequirements, consentValidityRulesSupplier, autoOnboardingService);
    }

    private AccountRequestService getAccountRequestService(ObjectMapper objectMapper,
                                                           AuthenticationService authenticationService) {
        return new DefaultAccountAccessConsentRequestService(authenticationService, getRestClient(objectMapper), ENDPOINT_VERSION);
    }

    private AuthenticationService getAuthenticationService(TidePropertiesV2 properties, Clock clock) {
        return new DefaultAuthenticationService(properties.getOAuthAuthorizationUrl(),
                new TideGroupPrivateKeyJwtOauth2ClientV2(properties.getOAuthTokenUrl(), new PrivateKeyJwtTokenBodyProducer(),
                        new DefaultClientAssertionProducer(new ExternalUserRequestTokenSigner(SIGNING_ALGORITHM), properties.getOAuthTokenUrl()), false),
                new ExternalUserRequestTokenSigner(SIGNING_ALGORITHM),
                getTokenClaimsProducer(properties),
                clock);
    }

    private DefaultFetchDataService getFetchDataService(ObjectMapper objectMapper,
                                                        Clock clock,
                                                        TidePropertiesV2 properties) {
        return new TideFetchDataServiceV1(getRestClient(objectMapper), properties, getTransactionMapper(), getDirectDebitMapper(),
                getStandingOrderMapper(), getAccountMapper(clock), new DefaultAccountFilter(),
                new TideSupportedAccountsSupplier(), CONSENT_WINDOW, ENDPOINT_VERSION, clock);
    }

    private TideGroupRestClientV2 getRestClient(ObjectMapper objectMapper) {
        ExternalPaymentNoB64RequestSigner noB64RequestSigner = new ExternalPaymentNoB64RequestSigner(objectMapper, SIGNING_ALGORITHM);
        return new TideGroupRestClientV2(new ExternalPaymentNoKidRequestSignerDecorator(objectMapper, SIGNING_ALGORITHM, noB64RequestSigner));
    }

    private TokenClaimsProducer getTokenClaimsProducer(TidePropertiesV2 properties) {
        return new DefaultTokenClaimsProducer(new TideGroupJwtClaimsProducerV1(DefaultAuthMeans::getClientId,
                properties.getAudience()));
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
                () -> Arrays.asList(INTERIMAVAILABLE),
                new DefaultCurrencyMapper(),
                new DefaultAccountIdMapper(),
                new TideAccountTypeMapper(),
                new CreditCardMapper(),
                new AccountNumberMapper(new DefaultSchemeMapper()),
                new DefaultAccountNameMapper(account -> "TIDE Account"),
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
