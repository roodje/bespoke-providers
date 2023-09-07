package com.yolt.providers.openbanking.ais.tidegroup.tide;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.openbanking.ais.common.HsmUtils;
import com.yolt.providers.openbanking.ais.generic2.DefaultProperties;
import com.yolt.providers.openbanking.ais.generic2.GenericBasePaymentProviderV3;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.claims.token.DefaultTokenClaimsProducer;
import com.yolt.providers.openbanking.ais.generic2.common.OpenBankingTokenScope;
import com.yolt.providers.openbanking.ais.generic2.common.ProviderIdentification;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import com.yolt.providers.openbanking.ais.generic2.http.DefaultHttpClientFactory;
import com.yolt.providers.openbanking.ais.generic2.oauth2.clientassertion.DefaultClientAssertionProducer;
import com.yolt.providers.openbanking.ais.generic2.oauth2.tokenbodysupplier.PrivateKeyJwtTokenBodyProducer;
import com.yolt.providers.openbanking.ais.generic2.pec.DefaultPaymentExecutionContextAdapterFactoryV2;
import com.yolt.providers.openbanking.ais.generic2.pec.common.UkProviderStateDeserializer;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.amount.DefaultAmountFormatter;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.datainitiation.PaymentDataInitiationMapper;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.instructionidentification.DateTimeAndUUIDBasedInstructionIdentificationSupplier;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.scheme.CamelCaseUkSchemeMapper;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.status.GenericConsentResponseStatusMapper;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.status.GenericStatusResponseStatusMapper;
import com.yolt.providers.openbanking.ais.generic2.service.AuthenticationService;
import com.yolt.providers.openbanking.ais.generic2.service.DefaultAuthenticationService;
import com.yolt.providers.openbanking.ais.generic2.signer.ExternalPaymentNoB64RequestSigner;
import com.yolt.providers.openbanking.ais.generic2.signer.ExternalUserRequestTokenSigner;
import com.yolt.providers.openbanking.ais.tidegroup.common.auth.TideGroupAuthMeansMapperV3;
import com.yolt.providers.openbanking.ais.tidegroup.common.claims.TideGroupJwtClaimsProducerV1;
import com.yolt.providers.openbanking.ais.tidegroup.common.oauth2.TideGroupPrivateKeyJwtOauth2ClientV2;
import com.yolt.providers.openbanking.ais.tidegroup.pis.validator.TidePaymentRequestValidator;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBRisk1;
import io.micrometer.core.instrument.MeterRegistry;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

import static com.yolt.providers.common.versioning.ProviderVersion.VERSION_1;
import static com.yolt.providers.openbanking.ais.danske.oauth2.DanskeAuthMeansBuilderV3.SIGNING_PRIVATE_KEY_ID_NAME;
import static com.yolt.providers.openbanking.ais.danske.oauth2.DanskeAuthMeansBuilderV3.TRANSPORT_PRIVATE_KEY_ID_NAME;

@Configuration
public class TidePaymentProviderBeanConfig {

    private static final String PROVIDER_KEY = "TIDE";
    private static final String DISPLAY_NAME = "TIDE";
    private static final String ENDPOINT_VERSION = "/v3.1";

    @Bean("TidePaymentProviderV1")
    public GenericBasePaymentProviderV3 tidePaymentProviderV1(TidePropertiesV2 properties,
                                                              MeterRegistry registry,
                                                              Clock clock,
                                                              @Qualifier("OpenBanking") ObjectMapper objectMapper) {
        ProviderIdentification providerIdentification = new ProviderIdentification(PROVIDER_KEY, DISPLAY_NAME, VERSION_1);
        AuthenticationService authenticationService = getAuthenticationService(properties, clock);

        TideGroupAuthMeansMapperV3 authMeansMapper = new TideGroupAuthMeansMapperV3();

        PaymentDataInitiationMapper ukDomesticDataInitiationMapper = new PaymentDataInitiationMapper(
                new DateTimeAndUUIDBasedInstructionIdentificationSupplier(clock),
                new DefaultAmountFormatter(),
                new CamelCaseUkSchemeMapper())
                .validateAfterMapWith(new TidePaymentRequestValidator());

        TokenScope tokenScope = TokenScope.builder()
                .grantScope(OpenBankingTokenScope.PAYMENTS.getGrantScope())
                .authorizationUrlScope(OpenBankingTokenScope.PAYMENTS.getAuthorizationUrlScope())
                .build();
        DefaultPaymentExecutionContextAdapterFactoryV2 executionContextAdapterFactory = new DefaultPaymentExecutionContextAdapterFactoryV2(
                providerIdentification,
                objectMapper,
                authMeansMapper.getAuthMeansMapper(PROVIDER_KEY),
                authenticationService,
                new DefaultHttpClientFactory(properties, registry, objectMapper),
                ukDomesticDataInitiationMapper,
                null,
                OBRisk1.PaymentContextCodeEnum.PARTYTOPARTY,
                () -> ENDPOINT_VERSION,
                new ExternalPaymentNoB64RequestSigner(objectMapper, AlgorithmIdentifiers.RSA_PSS_USING_SHA256),
                clock,
                tokenScope,
                new GenericConsentResponseStatusMapper(),
                new GenericStatusResponseStatusMapper(),
                null,
                null
        );

        return new GenericBasePaymentProviderV3(
                executionContextAdapterFactory.createInitiatePaymentExecutionContextAdapter(),
                null,
                executionContextAdapterFactory.createSubmitPaymentExecutionContextAdapter(),
                null,
                executionContextAdapterFactory.createStatusPaymentExecutionContextAdapter(),
                null,
                providerIdentification,
                () -> authMeansMapper.getTypedAuthMeans(),
                () -> HsmUtils.getKeyRequirements(SIGNING_PRIVATE_KEY_ID_NAME),
                () -> HsmUtils.getKeyRequirements(TRANSPORT_PRIVATE_KEY_ID_NAME),
                ConsentValidityRules.EMPTY_RULES_SET,
                new UkProviderStateDeserializer(objectMapper));
    }

    private AuthenticationService getAuthenticationService(DefaultProperties properties, Clock clock) {
        return new DefaultAuthenticationService(properties.getOAuthAuthorizationUrl(),
                new TideGroupPrivateKeyJwtOauth2ClientV2(properties.getOAuthTokenUrl(), new PrivateKeyJwtTokenBodyProducer(),
                        new DefaultClientAssertionProducer(new ExternalUserRequestTokenSigner(AlgorithmIdentifiers.RSA_PSS_USING_SHA256), properties.getOAuthTokenUrl()), false),
                new ExternalUserRequestTokenSigner(AlgorithmIdentifiers.RSA_PSS_USING_SHA256),
                new DefaultTokenClaimsProducer(new TideGroupJwtClaimsProducerV1(DefaultAuthMeans::getClientId,
                        properties.getAudience())),
                clock);
    }
}
