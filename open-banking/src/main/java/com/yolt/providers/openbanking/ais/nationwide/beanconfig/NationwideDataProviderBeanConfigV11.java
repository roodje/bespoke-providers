package com.yolt.providers.openbanking.ais.nationwide.beanconfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.openbanking.ais.common.HsmUtils;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.claims.producer.DefaultJwtClaimsProducer;
import com.yolt.providers.openbanking.ais.generic2.claims.token.DefaultTokenClaimsProducer;
import com.yolt.providers.openbanking.ais.generic2.common.OpenBankingTokenScope;
import com.yolt.providers.openbanking.ais.generic2.common.ProviderIdentification;
import com.yolt.providers.openbanking.ais.generic2.consentvalidity.DefaultConsentValidityRulesSupplier;
import com.yolt.providers.openbanking.ais.generic2.domain.LoginInfoState;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import com.yolt.providers.openbanking.ais.generic2.oauth2.BasicOauthClient;
import com.yolt.providers.openbanking.ais.generic2.oauth2.OneRefreshTokenDecorator;
import com.yolt.providers.openbanking.ais.generic2.oauth2.tokenbodysupplier.BasicOauthTokenBodyProducer;
import com.yolt.providers.openbanking.ais.generic2.oauth2.tokenbodysupplier.TokenRequestBodyProducer;
import com.yolt.providers.openbanking.ais.generic2.service.AuthenticationService;
import com.yolt.providers.openbanking.ais.generic2.service.DefaultAuthenticationService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.accountaccessconsentrequestservice.DefaultAccountAccessConsentRequestServiceV2;
import com.yolt.providers.openbanking.ais.generic2.service.ais.accountrequestservice.AccountRequestService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.accountrequestservice.DefaultConsentPermissions;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.DefaultAccountFilter;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.FetchDataServiceV2;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account.DefaultAccountIdMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account.DefaultAccountNameMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account.DefaultAccountNumberMapperV2;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account.DefaultAccountTypeMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account.schemesupport.DefaultSupportedSchemeAccountFilter;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.accountrefferencetypemapper.DefaultAccountReferenceTypeMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.amount.DefaultAmountParser;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.balance.DefaultBalanceMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.balanceamount.DefaultBalanceAmountMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.creditcard.CreditCardMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.currency.DefaultCurrencyMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.extendedaccount.DefaultExtendedAccountMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.extendedbalance.DefaultExtendedBalancesMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.extendedtransaction.DefaultExtendedTransactionMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.oauthtoken.DefaultAccessMeansStateMapper;
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
import com.yolt.providers.openbanking.ais.generic2.signer.ExternalPaymentNoB64RequestSigner;
import com.yolt.providers.openbanking.ais.generic2.signer.ExternalUserRequestTokenSigner;
import com.yolt.providers.openbanking.ais.nationwide.NationwideDataProviderV10;
import com.yolt.providers.openbanking.ais.nationwide.NationwidePropertiesV2;
import com.yolt.providers.openbanking.ais.nationwide.auth.NationwideAuthMeansBuilderV3;
import com.yolt.providers.openbanking.ais.nationwide.http.NationwideHttpClientFactoryV2;
import com.yolt.providers.openbanking.ais.nationwide.oauth2.NationwideAutoonboardingTokenBodyProducerDecorator;
import com.yolt.providers.openbanking.ais.nationwide.oauth2.NationwideMutualOauthTokenBodyProducerDecorator;
import com.yolt.providers.openbanking.ais.nationwide.service.ais.accountmapper.NationwideAccountMapperV7;
import com.yolt.providers.openbanking.ais.nationwide.service.ais.extendedaccountmapper.NationwideExtendedBalanceTypeMapper;
import com.yolt.providers.openbanking.ais.nationwide.service.ais.fetchdataservice.NationwideFetchDataServiceV9;
import com.yolt.providers.openbanking.ais.nationwide.service.autoonboarding.NationwideAutoOnboardingServiceV3;
import com.yolt.providers.openbanking.ais.nationwide.service.claims.NationwideExpiringJwtClaimsProducer;
import com.yolt.providers.openbanking.ais.nationwide.service.restclient.NationwideRestClientAisV7;
import com.yolt.providers.openbanking.ais.nationwide.service.restclient.NationwideRestClientPisV8;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBReadConsent1Data;
import io.micrometer.core.instrument.MeterRegistry;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.yolt.providers.openbanking.ais.generic2.service.ais.accountaccessconsentrequestservice.DefaultPermissions.DEFAULT_PERMISSIONS;
import static com.yolt.providers.openbanking.ais.nationwide.auth.NationwideAuthMeansBuilderV3.*;
import static com.yolt.providers.openbanking.dto.ais.openbanking316.OBBalanceType1Code.*;

@Configuration
public class NationwideDataProviderBeanConfigV11 {

    public static final String PROVIDER_KEY = "NATIONWIDE";
    public static final String DISPLAY_NAME = "Nationwide";
    private static final String ENDPOINT_VERSION = "/v3.1";

    private FetchDataServiceV2 getFetchDataService(NationwidePropertiesV2 properties,
                                                   final Clock clock,
                                                   @Qualifier("OpenBanking") final ObjectMapper objectMapper) {
        ZoneId zoneId = ZoneId.of("Europe/London");
        DefaultAmountParser amountParser = new DefaultAmountParser();
        DefaultAccountReferenceTypeMapper accountReferenceTypeMapper = new DefaultAccountReferenceTypeMapper();
        DefaultCurrencyMapper currencyCodeMapper = new DefaultCurrencyMapper();
        DefaultBalanceMapper balanceMapper = new DefaultBalanceMapper();
        DefaultSchemeMapper schemeMapper = new DefaultSchemeMapper();
        DefaultDateTimeMapper zonedDateTimeMapper = new DefaultDateTimeMapper(zoneId);
        DefaultBalanceAmountMapper balanceAmountMapper = new DefaultBalanceAmountMapper(currencyCodeMapper, balanceMapper);
        return new NationwideFetchDataServiceV9(
                new NationwideRestClientPisV8(new ExternalPaymentNoB64RequestSigner(objectMapper, AlgorithmIdentifiers.RSA_PSS_USING_SHA256)),
                new DefaultPartiesRestClient(),
                properties,
                new DefaultTransactionMapper(
                        new DefaultExtendedTransactionMapper(
                                accountReferenceTypeMapper,
                                new DefaultTransactionStatusMapper(),
                                balanceAmountMapper,
                                false,
                                zoneId),
                        zonedDateTimeMapper,
                        new PendingAsNullTransactionStatusMapper(),
                        amountParser,
                        new DefaultTransactionTypeMapper()),
                new DefaultDirectDebitMapper(zoneId, amountParser),
                new DefaultStandingOrderMapper(new DefaultPeriodMapper(), amountParser, schemeMapper, zonedDateTimeMapper),
                new DefaultPartiesMapper(),
                new NationwideAccountMapperV7(() -> Arrays.asList(INTERIMBOOKED), () -> Arrays.asList(INTERIMAVAILABLE),
                        () -> Arrays.asList(CLOSINGAVAILABLE, INTERIMCLEARED), () -> Arrays.asList(INTERIMAVAILABLE),
                        currencyCodeMapper,
                        new DefaultAccountIdMapper(),
                        new DefaultAccountTypeMapper(),
                        new CreditCardMapper(),
                        new DefaultAccountNumberMapperV2(schemeMapper),
                        new DefaultAccountNameMapper(account -> "Nationwide Account"),
                        balanceMapper,
                        new DefaultExtendedAccountMapper(accountReferenceTypeMapper, currencyCodeMapper,
                                new DefaultExtendedBalancesMapper(
                                        balanceAmountMapper,
                                        new NationwideExtendedBalanceTypeMapper(),
                                        zoneId)),
                        new DefaultSupportedSchemeAccountFilter(),
                        clock
                ),
                new DefaultAccountFilter(),
                ENDPOINT_VERSION,
                clock);
    }

    private TokenRequestBodyProducer getTokenRequestBodyProducer() {
        return new NationwideMutualOauthTokenBodyProducerDecorator(new BasicOauthTokenBodyProducer());
    }

    private TokenRequestBodyProducer getTokenRequestBodyProducerForAutoonboarding() {
        return new NationwideAutoonboardingTokenBodyProducerDecorator(new NationwideMutualOauthTokenBodyProducerDecorator(new BasicOauthTokenBodyProducer()));
    }

    private AuthenticationService getAuthenticationServiceV2(final NationwidePropertiesV2 properties,
                                                             final ExternalUserRequestTokenSigner tokenSigner,
                                                             TokenRequestBodyProducer tokenRequestBodyProducer,
                                                             final Clock clock,
                                                             boolean isInPisFlow) {
        return new DefaultAuthenticationService(properties.getOAuthAuthorizationUrl(),
                /**
                 * C4PO-9884 Due to FCA-SCA 90 days re-authentication exemption Nationwide decided to return refresh token only during very first SCA
                 * (grant_type=authorization_code). After 20.09.2022 perform action described in {@link OneRefreshTokenDecorator}
                 */
                new OneRefreshTokenDecorator(new BasicOauthClient(properties.getOAuthTokenUrl(), defaultAuthMeans -> null,
                        tokenRequestBodyProducer, isInPisFlow)),
                tokenSigner,
                new DefaultTokenClaimsProducer(new NationwideExpiringJwtClaimsProducer(new DefaultJwtClaimsProducer(DefaultAuthMeans::getClientId, properties.getAudience()), 60)),
                clock);
    }

    @Bean("NationwideDataProviderV11")
    public NationwideDataProviderV10 getNationwideV10DataProvider(final NationwidePropertiesV2 properties,
                                                                  @Qualifier("OpenBanking") final ObjectMapper objectMapper,
                                                                  final MeterRegistry registry,
                                                                  final Clock clock) {
        String jwsSigningAlgorithm = AlgorithmIdentifiers.RSA_PSS_USING_SHA256;
        ExternalUserRequestTokenSigner tokenSigner = new ExternalUserRequestTokenSigner(jwsSigningAlgorithm);
        AuthenticationService authenticationService = getAuthenticationServiceV2(properties, tokenSigner, getTokenRequestBodyProducer(), clock, false);
        AuthenticationService authenticationServiceForAutoonboarding = getAuthenticationServiceV2(properties, tokenSigner, getTokenRequestBodyProducerForAutoonboarding(), clock, false);
        var consentPersmissions = Stream.concat(Stream.of(OBReadConsent1Data.PermissionsEnum.READPARTY),
                DEFAULT_PERMISSIONS.stream()).collect(Collectors.toList());
        AccountRequestService accountRequestService = new DefaultAccountAccessConsentRequestServiceV2(
                authenticationService,
                new NationwideRestClientAisV7(
                        new ExternalPaymentNoB64RequestSigner(objectMapper, AlgorithmIdentifiers.RSA_PSS_USING_SHA256), properties),
                ENDPOINT_VERSION,
                consentPersmissions);
        TokenScope tokenScope = TokenScope.builder()
                .grantScope(OpenBankingTokenScope.ACCOUNTS.getGrantScope())
                .authorizationUrlScope(OpenBankingTokenScope.ACCOUNTS.getAuthorizationUrlScope())
                .build();
        return new NationwideDataProviderV10(
                getFetchDataService(properties, clock, objectMapper),
                accountRequestService,
                authenticationService,
                new NationwideHttpClientFactoryV2(properties, registry, objectMapper),
                tokenScope,
                new ProviderIdentification(PROVIDER_KEY, DISPLAY_NAME, ProviderVersion.VERSION_11),
                NationwideAuthMeansBuilderV3::createNationwideAuthenticationMeans,
                NationwideAuthMeansBuilderV3.getTypedAuthenticationMeansForAIS(),
                new DefaultAccessMeansStateMapper(objectMapper),
                new DefaultAccessMeansStateProvider(),
                () -> HsmUtils.getKeyRequirements(SIGNING_PRIVATE_KEY_ID_NAME),
                () -> HsmUtils.getKeyRequirements(TRANSPORT_PRIVATE_KEY_ID_NAME, TRANSPORT_CERTIFICATE_NAME),
                new NationwideAutoOnboardingServiceV3(new NationwideRestClientAisV7(new ExternalPaymentNoB64RequestSigner(objectMapper, AlgorithmIdentifiers.RSA_PSS_USING_SHA256), properties),
                        properties, authenticationServiceForAutoonboarding),
                new DefaultConsentValidityRulesSupplier(),
                new DefaultLoginInfoStateMapper(objectMapper),
                permissions -> new LoginInfoState(((List<String>) permissions)),
                new DefaultConsentPermissions(consentPersmissions));
    }
}