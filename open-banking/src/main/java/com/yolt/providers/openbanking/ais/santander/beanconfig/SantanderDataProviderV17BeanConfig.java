package com.yolt.providers.openbanking.ais.santander.beanconfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.openbanking.ais.common.HsmUtils;
import com.yolt.providers.openbanking.ais.generic2.GenericBaseDataProviderV2;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.claims.token.DefaultTokenClaimsProducer;
import com.yolt.providers.openbanking.ais.generic2.claims.token.TokenClaimsProducer;
import com.yolt.providers.openbanking.ais.generic2.common.OpenBankingTokenScope;
import com.yolt.providers.openbanking.ais.generic2.common.ProviderIdentification;
import com.yolt.providers.openbanking.ais.generic2.domain.LoginInfoState;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClientFactory;
import com.yolt.providers.openbanking.ais.generic2.oauth2.BasicOauthClient;
import com.yolt.providers.openbanking.ais.generic2.service.AuthenticationService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.accountaccessconsentrequestservice.DefaultAccountAccessConsentRequestServiceV2;
import com.yolt.providers.openbanking.ais.generic2.service.ais.accountrequestservice.AccountRequestService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.accountrequestservice.DefaultConsentPermissions;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.DefaultAccountFilter;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.DefaultConsentWindow;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.FetchDataServiceV2;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.supportedaccounts.DefaultSupportedAccountsSupplier;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account.*;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account.schemesupport.DefaultSupportedSchemeAccountFilter;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.accountrefferencetypemapper.DefaultAccountReferenceTypeMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.amount.DefaultAmountParser;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.balanceamount.BalanceAmountMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.balanceamount.DefaultBalanceAmountMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.balancetype.DefaultBalanceTypeMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.creditcard.CreditCardMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.currency.DefaultCurrencyMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.extendedaccount.DefaultExtendedAccountMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.extendedbalance.DefaultExtendedBalancesMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.extendedtransaction.DefaultExtendedTransactionMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.oauthtoken.AccessMeansStateMapper;
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
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transactionstatus.PendingAsNullTransactionStatusMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transactiontype.DefaultTransactionTypeMapper;
import com.yolt.providers.openbanking.ais.generic2.service.restclient.DefaultPartiesRestClient;
import com.yolt.providers.openbanking.ais.generic2.service.restclient.RestClient;
import com.yolt.providers.openbanking.ais.generic2.signer.ExternalPaymentNoB64RequestSigner;
import com.yolt.providers.openbanking.ais.generic2.signer.ExternalPaymentNoKidRequestSignerDecorator;
import com.yolt.providers.openbanking.ais.generic2.signer.ExternalUserRequestTokenSigner;
import com.yolt.providers.openbanking.ais.santander.SantanderPropertiesV2;
import com.yolt.providers.openbanking.ais.santander.auth.SantanderAuthMeansMapper;
import com.yolt.providers.openbanking.ais.santander.claims.SantanderJwtClaimsProducerV1;
import com.yolt.providers.openbanking.ais.santander.http.SantanderHttpClientFactory;
import com.yolt.providers.openbanking.ais.santander.oauth2.SantanderMutualTlsOauthClientV1;
import com.yolt.providers.openbanking.ais.santander.oauth2.SantanderOauthTokenBodyProducerV5;
import com.yolt.providers.openbanking.ais.santander.service.SantanderAuthenticationServiceV2;
import com.yolt.providers.openbanking.ais.santander.service.ais.fetchdataservice.SantanderFetchDataServiceV10;
import com.yolt.providers.openbanking.ais.santander.service.ais.mappers.SantanderAccessMeansStateMapper;
import com.yolt.providers.openbanking.ais.santander.service.ais.mappers.SantanderBalanceMapper;
import com.yolt.providers.openbanking.ais.santander.service.restclient.SantanderRestClientV8;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBReadConsent1Data;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.yolt.providers.common.versioning.ProviderVersion.VERSION_17;
import static com.yolt.providers.openbanking.ais.generic2.service.ais.accountaccessconsentrequestservice.DefaultPermissions.DEFAULT_PERMISSIONS;
import static com.yolt.providers.openbanking.ais.santander.auth.SantanderAuthMeansMapper.*;
import static com.yolt.providers.openbanking.dto.ais.openbanking316.OBBalanceType1Code.*;

@Configuration
class SantanderDataProviderV17BeanConfig {

    private static final String ENDPOINT_VERSION = "/v3.1";
    private static final String PROVIDER_KEY = "SANTANDER";
    private static final String DISPLAY_NAME = "Santander";
    private static final ZoneId ZONE_ID = ZoneId.of("Europe/London");

    @Bean("SantanderDataProviderV17")
    public GenericBaseDataProviderV2 getSantanderDataProviderV2(@Qualifier("OpenBanking") ObjectMapper objectMapper,
                                                                SantanderPropertiesV2 properties,
                                                                MeterRegistry meterRegistry,
                                                                Clock clock) {
        FetchDataServiceV2 fetchDataService = getFetchDataService(objectMapper, clock, properties);
        AuthenticationService authenticationService = getAuthenticationServiceV2(properties, clock);
        var consentPermissions = Stream.concat(Stream.of(OBReadConsent1Data.PermissionsEnum.READPARTY),
                DEFAULT_PERMISSIONS.stream()).collect(Collectors.toList());
        AccountRequestService accountRequestService = getAccountRequestService(objectMapper, authenticationService, consentPermissions);
        HttpClientFactory httpClientFactory = new SantanderHttpClientFactory(properties, meterRegistry, objectMapper);
        ProviderIdentification providerIdentification = new ProviderIdentification(PROVIDER_KEY, DISPLAY_NAME, VERSION_17);
        SantanderAuthMeansMapper authMeansMapper = new SantanderAuthMeansMapper();
        AccessMeansStateMapper accessMeansStateMapper = new SantanderAccessMeansStateMapper(objectMapper);
        Supplier<Optional<KeyRequirements>> signingKeyRequirements = () -> HsmUtils.getKeyRequirements(SIGNING_PRIVATE_KEY_ID_NAME);
        Supplier<Optional<KeyRequirements>> transportKeyRequirements = () -> HsmUtils.getKeyRequirements(TRANSPORT_PRIVATE_KEY_ID_NAME, TRANSPORT_CERTIFICATE_NAME);
        Supplier<ConsentValidityRules> consentValidityRulesSupplier = () -> new ConsentValidityRules(Collections.singleton("Santander UK - Open Banking"));
        TokenScope scope = TokenScope.builder()
                .grantScope(OpenBankingTokenScope.ACCOUNTS.getGrantScope())
                .authorizationUrlScope(OpenBankingTokenScope.ACCOUNTS.getAuthorizationUrlScope())
                .build();

        return new GenericBaseDataProviderV2(
                fetchDataService,
                accountRequestService,
                authenticationService,
                httpClientFactory,
                scope,
                providerIdentification,
                authMeansMapper.getAuthMeansMapperForAis(PROVIDER_KEY),
                authMeansMapper::getTypedAuthMeansForAis,
                accessMeansStateMapper,
                new DefaultAccessMeansStateProvider(),
                signingKeyRequirements,
                transportKeyRequirements,
                consentValidityRulesSupplier,
                new DefaultLoginInfoStateMapper(objectMapper),
                permissions -> new LoginInfoState((List<String>) permissions),
                new DefaultConsentPermissions(consentPermissions));
    }

    private AccountRequestService getAccountRequestService(ObjectMapper objectMapper,
                                                           AuthenticationService authenticationService,
                                                           List<OBReadConsent1Data.PermissionsEnum> consentPermissions) {
        return new DefaultAccountAccessConsentRequestServiceV2(
                authenticationService,
                getRestClient(objectMapper),
                ENDPOINT_VERSION,
                consentPermissions);
    }

    private AuthenticationService getAuthenticationServiceV2(SantanderPropertiesV2 properties, Clock clock) {
        BasicOauthClient oAuthClient = new SantanderMutualTlsOauthClientV1(properties,
                new SantanderOauthTokenBodyProducerV5(),
                false);
        return new SantanderAuthenticationServiceV2(properties.getOAuthAuthorizationUrl(),
                oAuthClient,
                new ExternalUserRequestTokenSigner(AlgorithmIdentifiers.RSA_PSS_USING_SHA256),
                getTokenClaimsProducer(properties),
                clock);
    }

    private FetchDataServiceV2 getFetchDataService(ObjectMapper objectMapper,
                                                   Clock clock,
                                                   SantanderPropertiesV2 properties) {
        return new SantanderFetchDataServiceV10(
                getRestClient(objectMapper),
                new DefaultPartiesRestClient(),
                properties,
                getTransactionMapper(),
                getDirectDebitMapper(),
                getStandingOrderMapper(),
                new DefaultPartiesMapper(),
                getAccountMapper(clock),
                new DefaultAccountFilter(),
                new DefaultSupportedAccountsSupplier(),
                DefaultConsentWindow.DURATION,
                ENDPOINT_VERSION,
                clock);
    }

    private RestClient getRestClient(ObjectMapper objectMapper) {
        ExternalPaymentNoB64RequestSigner noB64RequestSigner = new ExternalPaymentNoB64RequestSigner(objectMapper, AlgorithmIdentifiers.RSA_PSS_USING_SHA256);
        return new SantanderRestClientV8(new ExternalPaymentNoKidRequestSignerDecorator(objectMapper, AlgorithmIdentifiers.RSA_PSS_USING_SHA256, noB64RequestSigner));
    }

    private TokenClaimsProducer getTokenClaimsProducer(SantanderPropertiesV2 properties) {
        return new DefaultTokenClaimsProducer(new SantanderJwtClaimsProducerV1(DefaultAuthMeans::getClientId,
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

    private DefaultAccountMapperV3 getAccountMapper(Clock clock) {
        return new DefaultAccountMapperV3(
                () -> Arrays.asList(INTERIMBOOKED),
                () -> Arrays.asList(INTERIMAVAILABLE),
                () -> Arrays.asList(OPENINGCLEARED),
                () -> Arrays.asList(FORWARDAVAILABLE),
                new DefaultCurrencyMapper(),
                new DefaultAccountIdMapper(),
                new DefaultAccountTypeMapper(),
                new CreditCardMapper(),
                new DefaultAccountNumberMapperV2(new DefaultSchemeMapper()),
                new DefaultAccountNameMapper(account -> "Santander account"),
                new SantanderBalanceMapper(),
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
        return new DefaultBalanceAmountMapper(new DefaultCurrencyMapper(), new SantanderBalanceMapper());
    }
}
