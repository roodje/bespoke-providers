package com.yolt.providers.openbanking.ais.barclaysgroup.barclays.beanconfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.openbanking.ais.barclaysgroup.barclays.config.BarclaysPropertiesV3;
import com.yolt.providers.openbanking.ais.barclaysgroup.common.BarclaysGroupDynamicFormBaseDataProviderV4;
import com.yolt.providers.openbanking.ais.barclaysgroup.common.auth.BarclaysGroupAuthMeansBuilderV3;
import com.yolt.providers.openbanking.ais.barclaysgroup.common.claims.BarclaysGroupJwtTokenBodyProducer;
import com.yolt.providers.openbanking.ais.barclaysgroup.common.claims.producer.BarclaysGroupJwtClaimsProducerV2;
import com.yolt.providers.openbanking.ais.barclaysgroup.common.service.BarclaysGroupAuthenticationServiceV4;
import com.yolt.providers.openbanking.ais.barclaysgroup.common.service.ais.fetchdataservice.BarclaysGroupAccountFilter;
import com.yolt.providers.openbanking.ais.barclaysgroup.common.service.ais.fetchdataservice.BarclaysGroupFetchDataServiceV3;
import com.yolt.providers.openbanking.ais.barclaysgroup.common.service.ais.mappers.balance.BarlcaysGroupAvailableCreditCardBalanceMapperV2;
import com.yolt.providers.openbanking.ais.barclaysgroup.common.service.ais.mappers.extendedtransaction.BarclaysGroupExtendedTransactionMapper;
import com.yolt.providers.openbanking.ais.barclaysgroup.common.service.ais.mappers.transaction.BarclaysGroupTransactionMapper;
import com.yolt.providers.openbanking.ais.barclaysgroup.common.service.restclient.BarclaysGroupRestClientV5;
import com.yolt.providers.openbanking.ais.barclaysgroup.common.signer.BarclaysGroupPaymentRequestSignerV3;
import com.yolt.providers.openbanking.ais.barclaysgroup.common.signer.BarclaysGroupUserRequestTokenSignerV2;
import com.yolt.providers.openbanking.ais.common.HsmUtils;
import com.yolt.providers.openbanking.ais.generic2.GenericBaseDataProviderV2;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.claims.producer.ExpiringJwtClaimsProducerDecorator;
import com.yolt.providers.openbanking.ais.generic2.claims.token.DefaultTokenClaimsProducer;
import com.yolt.providers.openbanking.ais.generic2.common.OpenBankingTokenScope;
import com.yolt.providers.openbanking.ais.generic2.common.ProviderIdentification;
import com.yolt.providers.openbanking.ais.generic2.domain.LoginInfoState;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import com.yolt.providers.openbanking.ais.generic2.dto.PartyDto;
import com.yolt.providers.openbanking.ais.generic2.http.DefaultHttpClientFactory;
import com.yolt.providers.openbanking.ais.generic2.oauth2.clientassertion.DefaultClientAssertionProducer;
import com.yolt.providers.openbanking.ais.generic2.oauth2.implementations.PrivateKeyJwtOauth2Client;
import com.yolt.providers.openbanking.ais.generic2.service.ais.accountaccessconsentrequestservice.DefaultAccountAccessConsentRequestServiceV2;
import com.yolt.providers.openbanking.ais.generic2.service.ais.accountrequestservice.DefaultConsentPermissions;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.DefaultConsentWindow;
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
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.oauthtoken.DefaultAccessMeansStateMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.oauthtoken.DefaultAccessMeansStateProvider;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.oauthtoken.DefaultLoginInfoStateMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.period.DefaultPeriodMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.scheme.DefaultSchemeMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.time.DefaultDateTimeMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transaction.DefaultDirectDebitMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transaction.DefaultStandingOrderMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transactionstatus.DefaultTransactionStatusMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transactionstatus.PendingAsNullTransactionStatusMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transactiontype.DefaultTransactionTypeMapper;
import com.yolt.providers.openbanking.ais.generic2.service.restclient.DefaultPartiesRestClient;
import com.yolt.providers.openbanking.ais.generic2.service.restclient.DefaultRestClient;
import com.yolt.providers.openbanking.ais.generic2.signer.UserRequestTokenSigner;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBReadConsent1Data;
import io.micrometer.core.instrument.MeterRegistry;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.yolt.providers.common.versioning.ProviderVersion.VERSION_16;
import static com.yolt.providers.openbanking.ais.barclaysgroup.common.auth.BarclaysGroupAuthMeansBuilderV3.*;
import static com.yolt.providers.openbanking.ais.generic2.service.ais.accountaccessconsentrequestservice.DefaultPermissions.DEFAULT_PERMISSIONS;
import static com.yolt.providers.openbanking.dto.ais.openbanking316.OBBalanceType1Code.*;

@Configuration
public class BarclaysDataProviderBeanConfig {

    private static final String JWS_SIGNING_ALGORITHM = AlgorithmIdentifiers.RSA_PSS_USING_SHA256;
    private static final String ACCOUNT_NAME_FALLBACK = "Barclays Account";
    private static final String ENDPOINT_VERSION = "/v3.1";
    private static final String IDENTIFIER = "BARCLAYS";
    private static final String DISPLAY_NAME = "Barclays";
    private static final ZoneId ZONE_ID = ZoneId.of("Europe/London");

    @Bean("BarclaysDataProviderV16")
    public GenericBaseDataProviderV2 getBarclaysDataProviderV16(BarclaysPropertiesV3 properties,
                                                                MeterRegistry registry,
                                                                Clock clock,
                                                                @Qualifier("BarclaysObjectMapperV2") ObjectMapper objectMapper) {
        var providerIdentification = new ProviderIdentification(IDENTIFIER, DISPLAY_NAME, VERSION_16);
        BarclaysGroupAuthenticationServiceV4 authenticationService = getAuthenticationServiceV4(properties, clock, false);
        var consentPermissions = Stream.concat(Stream.of(OBReadConsent1Data.PermissionsEnum.READPARTY),
                DEFAULT_PERMISSIONS.stream()).collect(Collectors.toList());
        return new BarclaysGroupDynamicFormBaseDataProviderV4(
                properties,
                getFetchDataServiceV2(properties, clock, objectMapper),
                new DefaultAccountAccessConsentRequestServiceV2(
                        authenticationService,
                        getRestClient(objectMapper),
                        ENDPOINT_VERSION,
                        consentPermissions),
                authenticationService,
                getHttpClientFactory(properties, registry, objectMapper),
                TokenScope.builder()
                        .grantScope(OpenBankingTokenScope.ACCOUNTS.getAuthorizationUrlScope())
                        .authorizationUrlScope(OpenBankingTokenScope.ACCOUNTS.getAuthorizationUrlScope())
                        .build(),
                providerIdentification,
                BarclaysGroupAuthMeansBuilderV3::createAuthenticationMeans,
                BarclaysGroupAuthMeansBuilderV3.getTypedAuthenticationMeans(),
                new DefaultAccessMeansStateMapper<>(objectMapper),
                new DefaultAccessMeansStateProvider(),
                () -> HsmUtils.getKeyRequirements(SIGNING_PRIVATE_KEY_ID_NAME_V2),
                () -> HsmUtils.getKeyRequirements(TRANSPORT_PRIVATE_KEY_ID_NAME_V2, TRANSPORT_CERTIFICATE_NAME_V2),
                () -> ConsentValidityRules.EMPTY_RULES_SET,
                new DefaultLoginInfoStateMapper<>(objectMapper),
                LoginInfoState::new,
                objectMapper,
                new DefaultConsentPermissions(consentPermissions),
                clock);
    }

    private FetchDataServiceV2 getFetchDataServiceV2(BarclaysPropertiesV3 properties,
                                                     final Clock clock,
                                                     final ObjectMapper objectMapper) {
        var accountReferenceTypeMapper = new DefaultAccountReferenceTypeMapper();
        var currencyCodeMapper = new DefaultCurrencyMapper();
        var zonedDateTimeMapper = new DefaultDateTimeMapper(ZONE_ID);
        var amountParser = new DefaultAmountParser();
        var schemeMapper = new DefaultSchemeMapper();
        var defaultBalanceMapper = new DefaultBalanceMapper();
        return new BarclaysGroupFetchDataServiceV3(
                getRestClient(objectMapper),
                new DefaultPartiesRestClient(),
                properties,
                new BarclaysGroupTransactionMapper(
                        new BarclaysGroupExtendedTransactionMapper(
                                accountReferenceTypeMapper,
                                new DefaultTransactionStatusMapper(),
                                new DefaultBalanceAmountMapper(currencyCodeMapper, new DefaultBalanceMapper()),
                                true,
                                ZONE_ID
                        ),
                        zonedDateTimeMapper,
                        new PendingAsNullTransactionStatusMapper(),
                        amountParser,
                        new DefaultTransactionTypeMapper()
                ),
                new DefaultDirectDebitMapper(ZONE_ID, amountParser),
                new DefaultStandingOrderMapper(new DefaultPeriodMapper(), amountParser, schemeMapper, zonedDateTimeMapper),
                party -> new PartyDto(party.getFullLegalName()),
                new DefaultAccountMapperV3(
                        () -> Arrays.asList(EXPECTED, CLOSINGBOOKED),
                        () -> Arrays.asList(EXPECTED, INTERIMAVAILABLE),
                        () -> Collections.singletonList(INTERIMBOOKED),
                        () -> Collections.singletonList(INTERIMBOOKED),
                        currencyCodeMapper,
                        new DefaultAccountIdMapper(),
                        new DefaultAccountTypeMapper(),
                        new CreditCardMapper(),
                        new DefaultAccountNumberMapperV2(schemeMapper),
                        new DefaultAccountNameMapper(account -> ACCOUNT_NAME_FALLBACK),
                        defaultBalanceMapper,
                        new BarlcaysGroupAvailableCreditCardBalanceMapperV2(),
                        defaultBalanceMapper,
                        defaultBalanceMapper,
                        new DefaultExtendedAccountMapper(accountReferenceTypeMapper,
                                currencyCodeMapper,
                                new DefaultExtendedBalancesMapper(
                                        new DefaultBalanceAmountMapper(currencyCodeMapper, defaultBalanceMapper),
                                        new DefaultBalanceTypeMapper(),
                                        ZONE_ID)
                        ),
                        new DefaultSupportedSchemeAccountFilter(),
                        clock
                ),
                new BarclaysGroupAccountFilter(),
                new DefaultSupportedAccountsSupplier(),
                DefaultConsentWindow.DURATION,
                ENDPOINT_VERSION,
                clock);
    }

    public BarclaysGroupAuthenticationServiceV4 getAuthenticationServiceV4(final BarclaysPropertiesV3 properties,
                                                                           final Clock clock,
                                                                           final boolean isInPisFlow) {
        UserRequestTokenSigner userRequestTokenSigner = new BarclaysGroupUserRequestTokenSignerV2(JWS_SIGNING_ALGORITHM);
        return new BarclaysGroupAuthenticationServiceV4(
                properties,
                new PrivateKeyJwtOauth2Client<>(properties.getOAuthTokenUrl(),
                        new BarclaysGroupJwtTokenBodyProducer(),
                        new DefaultClientAssertionProducer(userRequestTokenSigner, properties.getOAuthTokenUrl()),
                        isInPisFlow),
                userRequestTokenSigner,
                new DefaultTokenClaimsProducer(new ExpiringJwtClaimsProducerDecorator(
                        new BarclaysGroupJwtClaimsProducerV2(DefaultAuthMeans::getClientId, properties.getAudience()),
                        5)),
                clock
        );
    }

    private DefaultRestClient getRestClient(ObjectMapper objectMapper) {
        return new BarclaysGroupRestClientV5(
                new BarclaysGroupPaymentRequestSignerV3(
                        objectMapper,
                        JWS_SIGNING_ALGORITHM));
    }

    private DefaultHttpClientFactory getHttpClientFactory(BarclaysPropertiesV3 properties,
                                                          MeterRegistry registry,
                                                          ObjectMapper objectMapper) {
        return new DefaultHttpClientFactory(properties, registry, objectMapper);
    }
}
