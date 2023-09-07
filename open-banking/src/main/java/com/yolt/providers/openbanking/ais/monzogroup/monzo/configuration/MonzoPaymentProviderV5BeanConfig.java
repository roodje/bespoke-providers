package com.yolt.providers.openbanking.ais.monzogroup.monzo.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.openbanking.ais.common.HsmUtils;
import com.yolt.providers.openbanking.ais.generic2.DefaultProperties;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.claims.producer.DefaultJwtClaimsProducer;
import com.yolt.providers.openbanking.ais.generic2.claims.token.DefaultTokenClaimsProducer;
import com.yolt.providers.openbanking.ais.generic2.claims.token.TokenClaimsProducer;
import com.yolt.providers.openbanking.ais.generic2.common.OpenBankingTokenScope;
import com.yolt.providers.openbanking.ais.generic2.common.ProviderIdentification;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import com.yolt.providers.openbanking.ais.generic2.pec.DefaultPaymentExecutionContextAdapterFactory;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.amount.DefaultAmountFormatter;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.datainitiation.PaymentDataInitiationMapper;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.instructionidentification.DateTimeAndUUIDBasedInstructionIdentificationSupplier;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.scheme.CamelCaseUkSchemeMapper;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.status.GenericConsentResponseStatusMapper;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.status.GenericStatusResponseStatusMapper;
import com.yolt.providers.openbanking.ais.generic2.service.AuthenticationService;
import com.yolt.providers.openbanking.ais.generic2.service.DefaultAuthenticationService;
import com.yolt.providers.openbanking.ais.generic2.signer.ExternalUserRequestTokenSigner;
import com.yolt.providers.openbanking.ais.generic2.signer.PaymentRequestSigner;
import com.yolt.providers.openbanking.ais.monzogroup.common.MonzoGroupBasePaymentProviderV2;
import com.yolt.providers.openbanking.ais.monzogroup.common.auth.MonzoGroupAuthMeansMapper;
import com.yolt.providers.openbanking.ais.monzogroup.common.http.MonzoGroupHttpClientFactoryV2;
import com.yolt.providers.openbanking.ais.monzogroup.common.http.paymenthttppayloadsigner.MonzoGroupExternalPaymentRequestSignerV2;
import com.yolt.providers.openbanking.ais.monzogroup.common.oauth2.MonzoMutualTlsOauth2Client;
import com.yolt.providers.openbanking.ais.monzogroup.common.service.MonzoGroupRegistrationServiceV2;
import com.yolt.providers.openbanking.ais.monzogroup.common.service.pis.MonzoGroupPaymentRequestValidator;
import com.yolt.providers.openbanking.ais.monzogroup.common.service.restclient.MonzoGroupRegistrationRestClientV2;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBRisk1;
import io.micrometer.core.instrument.MeterRegistry;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.function.Function;

import static com.yolt.providers.common.versioning.ProviderVersion.VERSION_5;
import static com.yolt.providers.openbanking.ais.monzogroup.common.auth.MonzoGroupAuthMeansMapper.*;

@Configuration
public class MonzoPaymentProviderV5BeanConfig {

    private static final String ENDPOINT_VERSION = "/v3.1";
    private static final String PROVIDER_KEY = "MONZO";
    private static final String DISPLAY_NAME = "Monzo";
    private static final String SIGNING_ALGORITHM = AlgorithmIdentifiers.RSA_PSS_USING_SHA256;
    private static final String LOCAL_INSTRUMENT = "UK.OBIE.FPS";

    @Bean("MonzoPaymentProviderV5")
    public MonzoGroupBasePaymentProviderV2 getMonzoPaymentProvider(@Qualifier("OpenBanking") ObjectMapper objectMapper,
                                                                   MonzoPropertiesV2 properties,
                                                                   MeterRegistry meterRegistry,
                                                                   Clock clock) {
        AuthenticationService authenticationService = getAuthenticationService(properties, clock);
        MonzoGroupHttpClientFactoryV2 httpClientFactory = new MonzoGroupHttpClientFactoryV2(properties, meterRegistry, objectMapper);
        ProviderIdentification providerIdentification = new ProviderIdentification(PROVIDER_KEY, DISPLAY_NAME, VERSION_5);
        MonzoGroupAuthMeansMapper authMeansMapper = new MonzoGroupAuthMeansMapper();
        ConsentValidityRules consentValidityRules = new ConsentValidityRules(new HashSet<>(Arrays.asList(
                "Account Number of your Monzo account",
                "Sort Code of your Monzo account",
                "Email address on your Monzo account",
                "Continue"
        )));
        MonzoGroupRegistrationServiceV2 registrationService = new MonzoGroupRegistrationServiceV2(getRestRegistrationClient(objectMapper), properties);
        Function<Map<String, BasicAuthenticationMean>, DefaultAuthMeans> authMeansProvider =
                authMeansMapper.getAuthMeansMapper(PROVIDER_KEY);
        DateTimeAndUUIDBasedInstructionIdentificationSupplier instructionIdentificationSupplier = new DateTimeAndUUIDBasedInstructionIdentificationSupplier(clock);
        PaymentDataInitiationMapper ukDomesticDataInitiationMapper = new PaymentDataInitiationMapper(
                instructionIdentificationSupplier,
                new DefaultAmountFormatter(),
                new CamelCaseUkSchemeMapper()
        )
                .withDebtorAccount()
                .withLocalInstrument(LOCAL_INSTRUMENT)
                .validateAfterMapWith(new MonzoGroupPaymentRequestValidator());

        TokenScope tokenScope = TokenScope.builder()
                .grantScope(OpenBankingTokenScope.PAYMENTS.getGrantScope())
                .authorizationUrlScope(OpenBankingTokenScope.PAYMENTS.getAuthorizationUrlScope())
                .build();
        DefaultPaymentExecutionContextAdapterFactory executionContextAdapterFactory = new DefaultPaymentExecutionContextAdapterFactory(
                providerIdentification,
                objectMapper,
                authMeansProvider,
                authenticationService,
                httpClientFactory,
                ukDomesticDataInitiationMapper,
                OBRisk1.PaymentContextCodeEnum.PARTYTOPARTY,
                () -> ENDPOINT_VERSION,
                getPaymentRequestSigner(objectMapper),
                clock,
                tokenScope,
                new GenericConsentResponseStatusMapper(),
                new GenericStatusResponseStatusMapper()
        );

        return new MonzoGroupBasePaymentProviderV2(
                httpClientFactory,
                providerIdentification,
                authMeansMapper.getAuthMeansMapper(PROVIDER_KEY),
                authMeansMapper::getTypedAuthMeans,
                () -> HsmUtils.getKeyRequirements(SIGNING_PRIVATE_KEY_ID_NAME),
                () -> HsmUtils.getKeyRequirements(TRANSPORT_PRIVATE_KEY_ID_NAME, TRANSPORT_CERTIFICATE_NAME),
                consentValidityRules,
                registrationService,
                executionContextAdapterFactory,
                tokenScope);
    }

    private AuthenticationService getAuthenticationService(DefaultProperties properties, Clock clock) {
        return new DefaultAuthenticationService(properties.getOAuthAuthorizationUrl(),
                new MonzoMutualTlsOauth2Client(properties, true),
                new ExternalUserRequestTokenSigner(SIGNING_ALGORITHM),
                getTokenClaimsProducer(properties),
                clock);
    }

    private MonzoGroupRegistrationRestClientV2 getRestRegistrationClient(ObjectMapper objectMapper) {
        return new MonzoGroupRegistrationRestClientV2(getPaymentRequestSigner(objectMapper));
    }

    private PaymentRequestSigner getPaymentRequestSigner(ObjectMapper objectMapper) {
        return new MonzoGroupExternalPaymentRequestSignerV2(objectMapper, SIGNING_ALGORITHM);
    }

    private TokenClaimsProducer getTokenClaimsProducer(DefaultProperties properties) {
        return new DefaultTokenClaimsProducer(new DefaultJwtClaimsProducer(DefaultAuthMeans::getClientId,
                properties.getAudience()));
    }
}
