package com.yolt.providers.openbanking.ais.santander.beanconfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.openbanking.ais.common.HsmUtils;
import com.yolt.providers.openbanking.ais.generic2.GenericBasePaymentProviderV2;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.claims.token.DefaultTokenClaimsProducer;
import com.yolt.providers.openbanking.ais.generic2.claims.token.TokenClaimsProducer;
import com.yolt.providers.openbanking.ais.generic2.common.OpenBankingTokenScope;
import com.yolt.providers.openbanking.ais.generic2.common.ProviderIdentification;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import com.yolt.providers.openbanking.ais.generic2.http.DefaultHttpClientFactory;
import com.yolt.providers.openbanking.ais.generic2.oauth2.BasicOauthClient;
import com.yolt.providers.openbanking.ais.generic2.pec.DefaultPaymentExecutionContextAdapterFactory;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.amount.DefaultAmountFormatter;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.datainitiation.PaymentDataInitiationMapper;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.instructionidentification.DateTimeAndUUIDBasedInstructionIdentificationSupplier;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.scheme.CamelCaseUkSchemeMapper;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.status.GenericConsentResponseStatusMapper;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.status.GenericStatusResponseStatusMapper;
import com.yolt.providers.openbanking.ais.generic2.service.AuthenticationService;
import com.yolt.providers.openbanking.ais.generic2.signer.ExternalPaymentNoB64RequestSigner;
import com.yolt.providers.openbanking.ais.generic2.signer.ExternalUserRequestTokenSigner;
import com.yolt.providers.openbanking.ais.santander.SantanderPropertiesV2;
import com.yolt.providers.openbanking.ais.santander.auth.SantanderAuthMeansMapper;
import com.yolt.providers.openbanking.ais.santander.claims.SantanderJwtClaimsProducerV1;
import com.yolt.providers.openbanking.ais.santander.oauth2.SantanderMutualTlsOauthClientV1;
import com.yolt.providers.openbanking.ais.santander.oauth2.SantanderOauthTokenBodyProducerV5;
import com.yolt.providers.openbanking.ais.santander.service.SantanderAuthenticationServiceV2;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBRisk1;
import io.micrometer.core.instrument.MeterRegistry;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

import static com.yolt.providers.openbanking.ais.santander.auth.SantanderAuthMeansMapper.*;

@Configuration
class SantanderPaymentProviderV15BeanConfig {

    private static final String ENDPOINT_VERSION = "/v3.1";
    private static final String PROVIDER_KEY = "SANTANDER";
    private static final String DISPLAY_NAME = "Santander";

    @Bean("SantanderPaymentProviderV15")
    public GenericBasePaymentProviderV2 getSantanderPaymentProviderV2(@Qualifier("OpenBanking") ObjectMapper objectMapper,
                                                                      SantanderPropertiesV2 properties,
                                                                      MeterRegistry meterRegistry,
                                                                      Clock clock) {
        AuthenticationService authenticationService = getAuthenticationServiceV2(properties, clock);
        ProviderIdentification providerIdentification = new ProviderIdentification(PROVIDER_KEY, DISPLAY_NAME, ProviderVersion.VERSION_15);
        DateTimeAndUUIDBasedInstructionIdentificationSupplier instructionIdentificationSupplier = new DateTimeAndUUIDBasedInstructionIdentificationSupplier(clock);
        PaymentDataInitiationMapper ukDomesticDataInitiationMapper = new PaymentDataInitiationMapper(instructionIdentificationSupplier,
                new DefaultAmountFormatter(),
                new CamelCaseUkSchemeMapper());

        SantanderAuthMeansMapper santanderAuthMeansMapper = new SantanderAuthMeansMapper();
        String jwsSigningAlgorithm = AlgorithmIdentifiers.RSA_PSS_USING_SHA256;
        TokenScope tokenScope = TokenScope.builder()
                .grantScope(OpenBankingTokenScope.PAYMENTS.getGrantScope())
                .authorizationUrlScope(OpenBankingTokenScope.PAYMENTS.getAuthorizationUrlScope())
                .build();
        DefaultPaymentExecutionContextAdapterFactory executionContextAdapterFactory = new DefaultPaymentExecutionContextAdapterFactory(
                providerIdentification,
                objectMapper,
                santanderAuthMeansMapper.getAuthMeansMapperForPis(PROVIDER_KEY),
                authenticationService,
                new DefaultHttpClientFactory(properties, meterRegistry, objectMapper),
                ukDomesticDataInitiationMapper,
                OBRisk1.PaymentContextCodeEnum.PARTYTOPARTY,
                () -> ENDPOINT_VERSION,
                new ExternalPaymentNoB64RequestSigner(objectMapper, jwsSigningAlgorithm),
                clock,
                tokenScope,
                new GenericConsentResponseStatusMapper(),
                new GenericStatusResponseStatusMapper()
        );

        return new GenericBasePaymentProviderV2(executionContextAdapterFactory.createInitiatePaymentExecutionContextAdapter(),
                executionContextAdapterFactory.createSubmitPaymentExecutionContextAdapter(),
                executionContextAdapterFactory.createStatusPaymentExecutionContextAdapter(),
                providerIdentification,
                santanderAuthMeansMapper::getTypedAuthMeansForPis,
                () -> HsmUtils.getKeyRequirements(SIGNING_PRIVATE_KEY_ID_NAME),
                () -> HsmUtils.getKeyRequirements(TRANSPORT_PRIVATE_KEY_ID_NAME, TRANSPORT_CERTIFICATE_NAME),
                ConsentValidityRules.EMPTY_RULES_SET);
    }

    private AuthenticationService getAuthenticationServiceV2(SantanderPropertiesV2 properties, Clock clock) {
        BasicOauthClient oAuthClient = new SantanderMutualTlsOauthClientV1(properties,
                new SantanderOauthTokenBodyProducerV5(),
                true);
        return new SantanderAuthenticationServiceV2(properties.getOAuthAuthorizationUrl(),
                oAuthClient,
                new ExternalUserRequestTokenSigner(AlgorithmIdentifiers.RSA_PSS_USING_SHA256),
                getTokenClaimsProducer(properties),
                clock);
    }

    private TokenClaimsProducer getTokenClaimsProducer(SantanderPropertiesV2 properties) {
        return new DefaultTokenClaimsProducer(new SantanderJwtClaimsProducerV1(DefaultAuthMeans::getClientId,
                properties.getAudience()));
    }
}
