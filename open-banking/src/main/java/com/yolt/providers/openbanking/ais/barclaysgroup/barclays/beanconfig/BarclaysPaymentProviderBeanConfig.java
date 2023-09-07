package com.yolt.providers.openbanking.ais.barclaysgroup.barclays.beanconfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.openbanking.ais.barclaysgroup.barclays.config.BarclaysPropertiesV3;
import com.yolt.providers.openbanking.ais.barclaysgroup.common.auth.BarclaysGroupAuthMeansBuilderV3;
import com.yolt.providers.openbanking.ais.barclaysgroup.common.claims.BarclaysGroupJwtTokenBodyProducer;
import com.yolt.providers.openbanking.ais.barclaysgroup.common.claims.producer.BarclaysGroupJwtClaimsProducerV2;
import com.yolt.providers.openbanking.ais.barclaysgroup.common.service.BarclaysGroupAuthenticationServiceV4;
import com.yolt.providers.openbanking.ais.barclaysgroup.common.service.pis.BarclaysUkDomesticPaymentRequestAdjuster;
import com.yolt.providers.openbanking.ais.barclaysgroup.common.service.pis.BarclaysUkDomesticPaymentRequestValidator;
import com.yolt.providers.openbanking.ais.barclaysgroup.common.signer.BarclaysGroupPaymentRequestSignerV3;
import com.yolt.providers.openbanking.ais.barclaysgroup.common.signer.BarclaysGroupUserRequestTokenSignerV2;
import com.yolt.providers.openbanking.ais.common.HsmUtils;
import com.yolt.providers.openbanking.ais.generic2.GenericBasePaymentProviderV2;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.claims.producer.ExpiringJwtClaimsProducerDecorator;
import com.yolt.providers.openbanking.ais.generic2.claims.token.DefaultTokenClaimsProducer;
import com.yolt.providers.openbanking.ais.generic2.common.OpenBankingTokenScope;
import com.yolt.providers.openbanking.ais.generic2.common.ProviderIdentification;
import com.yolt.providers.openbanking.ais.generic2.consentvalidity.DefaultConsentValidityRulesSupplier;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import com.yolt.providers.openbanking.ais.generic2.http.DefaultHttpClientFactory;
import com.yolt.providers.openbanking.ais.generic2.oauth2.clientassertion.DefaultClientAssertionProducer;
import com.yolt.providers.openbanking.ais.generic2.oauth2.implementations.PrivateKeyJwtOauth2Client;
import com.yolt.providers.openbanking.ais.generic2.pec.DefaultPaymentExecutionContextAdapterFactory;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.amount.DefaultAmountFormatter;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.datainitiation.PaymentDataInitiationMapper;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.instructionidentification.DateTimeAndUUIDBasedInstructionIdentificationSupplier;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.scheme.CamelCaseUkSchemeMapper;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.status.GenericConsentResponseStatusMapper;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.status.GenericStatusResponseStatusMapper;
import com.yolt.providers.openbanking.ais.generic2.service.AuthenticationService;
import com.yolt.providers.openbanking.ais.generic2.signer.PaymentRequestSigner;
import com.yolt.providers.openbanking.ais.generic2.signer.UserRequestTokenSigner;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBRisk1;
import io.micrometer.core.instrument.MeterRegistry;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

import static com.yolt.providers.common.versioning.ProviderVersion.VERSION_16;
import static com.yolt.providers.openbanking.ais.barclaysgroup.common.auth.BarclaysGroupAuthMeansBuilderV3.*;

@Configuration
public class BarclaysPaymentProviderBeanConfig {

    private static final String JWS_SIGNING_ALGORITHM = AlgorithmIdentifiers.RSA_PSS_USING_SHA256;
    private static final String ENDPOINT_VERSION = "/v3.1";
    private static final String IDENTIFIER = "BARCLAYS";
    private static final String DISPLAY_NAME = "Barclays";

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

    @Bean("BarclaysPaymentProviderV16")
    public GenericBasePaymentProviderV2 getBarclaysPaymentProviderV16(BarclaysPropertiesV3 properties,
                                                                      MeterRegistry registry,
                                                                      Clock clock,
                                                                      @Qualifier("OpenBanking") ObjectMapper objectMapper) {
        ProviderIdentification providerIdentification = new ProviderIdentification(IDENTIFIER, DISPLAY_NAME, VERSION_16);
        AuthenticationService authenticationService = getAuthenticationServiceV4(properties, clock, true);
        DateTimeAndUUIDBasedInstructionIdentificationSupplier instructionIdentificationSupplier =
                new DateTimeAndUUIDBasedInstructionIdentificationSupplier(clock);
        PaymentDataInitiationMapper ukDomesticDataInitiationMapper = new PaymentDataInitiationMapper(
                instructionIdentificationSupplier,
                new DefaultAmountFormatter(),
                new CamelCaseUkSchemeMapper()
        )
                .withDebtorAccount()
                .validateAfterMapWith(new BarclaysUkDomesticPaymentRequestValidator())
                .withAdjuster(new BarclaysUkDomesticPaymentRequestAdjuster());
        TokenScope tokenScope = TokenScope.builder()
                .grantScope(OpenBankingTokenScope.PAYMENTS.getAuthorizationUrlScope())
                .authorizationUrlScope(OpenBankingTokenScope.PAYMENTS.getAuthorizationUrlScope())
                .build();
        DefaultPaymentExecutionContextAdapterFactory executionContextAdapterFactory = new DefaultPaymentExecutionContextAdapterFactory(
                providerIdentification,
                objectMapper,
                BarclaysGroupAuthMeansBuilderV3::createAuthenticationMeans,
                authenticationService,
                getHttpClientFactory(properties, registry, objectMapper),
                ukDomesticDataInitiationMapper,
                OBRisk1.PaymentContextCodeEnum.PARTYTOPARTY,
                () -> ENDPOINT_VERSION,
                getPaymentRequestSigner(objectMapper),
                clock,
                tokenScope,
                new GenericConsentResponseStatusMapper(),
                new GenericStatusResponseStatusMapper()
        );
        return new GenericBasePaymentProviderV2(executionContextAdapterFactory.createInitiatePaymentExecutionContextAdapter(),
                executionContextAdapterFactory.createSubmitPaymentExecutionContextAdapter(),
                executionContextAdapterFactory.createStatusPaymentExecutionContextAdapter(),
                providerIdentification,
                BarclaysGroupAuthMeansBuilderV3.getTypedAuthenticationMeans(),
                () -> HsmUtils.getKeyRequirements(SIGNING_PRIVATE_KEY_ID_NAME_V2),
                () -> HsmUtils.getKeyRequirements(TRANSPORT_PRIVATE_KEY_ID_NAME_V2, TRANSPORT_CERTIFICATE_NAME_V2),
                new DefaultConsentValidityRulesSupplier().get()
        );
    }

    private PaymentRequestSigner getPaymentRequestSigner(ObjectMapper objectMapper) {
        return new BarclaysGroupPaymentRequestSignerV3(
                objectMapper,
                JWS_SIGNING_ALGORITHM);
    }

    private DefaultHttpClientFactory getHttpClientFactory(BarclaysPropertiesV3 properties,
                                                          MeterRegistry registry,
                                                          ObjectMapper objectMapper) {
        return new DefaultHttpClientFactory(properties, registry, objectMapper);
    }
}
