package com.yolt.providers.openbanking.ais.nationwide.beanconfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.openbanking.ais.common.HsmUtils;
import com.yolt.providers.openbanking.ais.generic2.GenericBasePaymentProviderV2;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.claims.producer.DefaultJwtClaimsProducer;
import com.yolt.providers.openbanking.ais.generic2.claims.token.DefaultTokenClaimsProducer;
import com.yolt.providers.openbanking.ais.generic2.common.OpenBankingTokenScope;
import com.yolt.providers.openbanking.ais.generic2.common.ProviderIdentification;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import com.yolt.providers.openbanking.ais.generic2.oauth2.BasicOauthClient;
import com.yolt.providers.openbanking.ais.generic2.oauth2.OneRefreshTokenDecorator;
import com.yolt.providers.openbanking.ais.generic2.oauth2.tokenbodysupplier.BasicOauthTokenBodyProducer;
import com.yolt.providers.openbanking.ais.generic2.oauth2.tokenbodysupplier.TokenRequestBodyProducer;
import com.yolt.providers.openbanking.ais.generic2.pec.DefaultPaymentExecutionContextAdapterFactory;
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
import com.yolt.providers.openbanking.ais.nationwide.NationwidePropertiesV2;
import com.yolt.providers.openbanking.ais.nationwide.auth.NationwideAuthMeansBuilderV3;
import com.yolt.providers.openbanking.ais.nationwide.http.NationwideHttpClientFactoryV2;
import com.yolt.providers.openbanking.ais.nationwide.oauth2.NationwideMutualOauthTokenBodyProducerDecorator;
import com.yolt.providers.openbanking.ais.nationwide.service.claims.NationwideExpiringJwtClaimsProducer;
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

import static com.yolt.providers.openbanking.ais.nationwide.auth.NationwideAuthMeansBuilderV3.*;

@Configuration
public class NationwidePaymentProviderBeanConfigV12 {

    public static final String PROVIDER_KEY = "NATIONWIDE";
    public static final String DISPLAY_NAME = "Nationwide";
    private static final String ENDPOINT_VERSION = "/v3.1";

    private TokenRequestBodyProducer getTokenRequestBodyProducer() {
        return new NationwideMutualOauthTokenBodyProducerDecorator(new BasicOauthTokenBodyProducer());
    }

    private AuthenticationService getAuthenticationServiceV2(final NationwidePropertiesV2 properties,
                                                             final ExternalUserRequestTokenSigner tokenSigner,
                                                             TokenRequestBodyProducer tokenRequestBodyProducer,
                                                             final Clock clock,
                                                             boolean isInPisFlow) {
        return new DefaultAuthenticationService(properties.getOAuthAuthorizationUrl(),
                new OneRefreshTokenDecorator(new BasicOauthClient<>(properties.getOAuthTokenUrl(), defaultAuthMeans -> null,
                        tokenRequestBodyProducer, isInPisFlow)),
                tokenSigner,
                new DefaultTokenClaimsProducer(new NationwideExpiringJwtClaimsProducer(new DefaultJwtClaimsProducer(DefaultAuthMeans::getClientId, properties.getAudience()), 60)),
                clock);
    }

    @Bean("NationwidePaymentProviderV12")
    public GenericBasePaymentProviderV2 getNationwidePaymentProvider(final NationwidePropertiesV2 properties,
                                                                     @Qualifier("OpenBanking") final ObjectMapper objectMapper,
                                                                     final MeterRegistry registry,
                                                                     final Clock clock) {
        ProviderIdentification providerIdentification = new ProviderIdentification(PROVIDER_KEY, DISPLAY_NAME, ProviderVersion.VERSION_12);
        Function<Map<String, BasicAuthenticationMean>, DefaultAuthMeans> authMeansProvider =
                NationwideAuthMeansBuilderV3::createNationwideAuthenticationMeans;
        DateTimeAndUUIDBasedInstructionIdentificationSupplier instructionIdentificationSupplier = new DateTimeAndUUIDBasedInstructionIdentificationSupplier(clock);
        PaymentDataInitiationMapper ukDomesticDataInitiationMapper = new PaymentDataInitiationMapper(instructionIdentificationSupplier,
                new DefaultAmountFormatter(),
                new CamelCaseUkSchemeMapper())
                .withDebtorAccount();

        String jwsSigningAlgorithm = AlgorithmIdentifiers.RSA_PSS_USING_SHA256;
        ExternalUserRequestTokenSigner tokenSigner = new ExternalUserRequestTokenSigner(jwsSigningAlgorithm);
        AuthenticationService authenticationService = getAuthenticationServiceV2(properties, tokenSigner, getTokenRequestBodyProducer(), clock, true);
        TokenScope tokenScope = TokenScope.builder()
                .grantScope(OpenBankingTokenScope.PAYMENTS.getGrantScope())
                .authorizationUrlScope(OpenBankingTokenScope.PAYMENTS.getAuthorizationUrlScope())
                .build();
        DefaultPaymentExecutionContextAdapterFactory executionContextAdapterFactory = new DefaultPaymentExecutionContextAdapterFactory(
                providerIdentification,
                objectMapper,
                authMeansProvider,
                authenticationService,
                new NationwideHttpClientFactoryV2(properties, registry, objectMapper),
                ukDomesticDataInitiationMapper,
                OBRisk1.PaymentContextCodeEnum.OTHER,
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
                getTypedAuthenticationMeansForPIS(),
                () -> HsmUtils.getKeyRequirements(SIGNING_PRIVATE_KEY_ID_NAME),
                () -> HsmUtils.getKeyRequirements(TRANSPORT_PRIVATE_KEY_ID_NAME),
                new ConsentValidityRules(new HashSet<>(Arrays.asList(
                        "Customer number",
                        "Date of birth",
                        "Continue",
                        "This payment will be made using Open Banking"
                ))));
    }
}