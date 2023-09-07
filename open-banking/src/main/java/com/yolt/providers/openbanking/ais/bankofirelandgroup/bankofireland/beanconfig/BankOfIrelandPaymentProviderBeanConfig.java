package com.yolt.providers.openbanking.ais.bankofirelandgroup.bankofireland.beanconfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.openbanking.ais.bankofirelandgroup.bankofireland.BankOfIrelandProperties;
import com.yolt.providers.openbanking.ais.bankofirelandgroup.bankofireland.auth.BankOfIrelandAuthMeansMapper;
import com.yolt.providers.openbanking.ais.bankofirelandgroup.common.oauth2.BankOfIrelandOauthTokenBodyProducer;
import com.yolt.providers.openbanking.ais.bankofirelandgroup.common.service.BankOfIrelandGroupAuthenticationService;
import com.yolt.providers.openbanking.ais.bankofirelandgroup.common.service.pis.BankOfIrelandGroupPaymentRequestValidator;
import com.yolt.providers.openbanking.ais.bankofirelandgroup.common.service.pis.BankOfIrelandPaymentRequestSigner;
import com.yolt.providers.openbanking.ais.common.HsmUtils;
import com.yolt.providers.openbanking.ais.generic2.DefaultProperties;
import com.yolt.providers.openbanking.ais.generic2.GenericBasePaymentProviderV3;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.claims.producer.DefaultJwtClaimsProducer;
import com.yolt.providers.openbanking.ais.generic2.claims.producer.ExpiringJwtClaimsProducerDecorator;
import com.yolt.providers.openbanking.ais.generic2.claims.token.DefaultTokenClaimsProducer;
import com.yolt.providers.openbanking.ais.generic2.claims.token.TokenClaimsProducer;
import com.yolt.providers.openbanking.ais.generic2.common.OpenBankingTokenScope;
import com.yolt.providers.openbanking.ais.generic2.common.ProviderIdentification;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import com.yolt.providers.openbanking.ais.generic2.http.DefaultHttpClientFactory;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClientFactory;
import com.yolt.providers.openbanking.ais.generic2.oauth2.implementations.DefaultMutualTlsOauth2Client;
import com.yolt.providers.openbanking.ais.generic2.pec.DefaultPaymentExecutionContextAdapterFactoryV2;
import com.yolt.providers.openbanking.ais.generic2.pec.common.UkProviderStateDeserializer;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.amount.DefaultAmountFormatter;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.datainitiation.PaymentDataInitiationMapper;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.instructionidentification.DateTimeAndUUIDBasedInstructionIdentificationSupplier;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.scheme.CamelCaseUkSchemeMapper;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.status.GenericConsentResponseStatusMapper;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.status.GenericScheduledConsentResponseStatusMapper;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.status.GenericScheduledResponseStatusMapper;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.status.GenericStatusResponseStatusMapper;
import com.yolt.providers.openbanking.ais.generic2.service.AuthenticationService;
import com.yolt.providers.openbanking.ais.generic2.signer.ExternalUserRequestTokenSigner;
import com.yolt.providers.openbanking.ais.generic2.signer.PaymentRequestSigner;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBRisk1;
import io.micrometer.core.instrument.MeterRegistry;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

import static com.yolt.providers.common.versioning.ProviderVersion.VERSION_1;
import static com.yolt.providers.openbanking.ais.bankofirelandgroup.bankofireland.beanconfig.BankOfIrelandDetailsProvider.BANK_OF_IRELAND_PROVIDER_KEY;
import static com.yolt.providers.openbanking.ais.bankofirelandgroup.bankofireland.beanconfig.BankOfIrelandDetailsProvider.BANK_OF_IRELAND_PROVIDER_NAME;
import static com.yolt.providers.openbanking.ais.hsbcgroup.common.auth.HsbcGroupAuthMeansBuilderV3.*;

@Configuration
public class BankOfIrelandPaymentProviderBeanConfig {

    private static final String ENDPOINT_VERSION = "/v3.0";

    private static final String SIGNING_ALGORITHM = AlgorithmIdentifiers.RSA_PSS_USING_SHA256;

    @Bean("BankOfIrelandPaymentProviderV1")
    public GenericBasePaymentProviderV3 createPaymentProvider(BankOfIrelandProperties properties,
                                                              MeterRegistry registry,
                                                              Clock clock,
                                                              @Qualifier("OpenBanking") ObjectMapper objectMapper) {

        ProviderIdentification providerIdentification = new ProviderIdentification(BANK_OF_IRELAND_PROVIDER_KEY, BANK_OF_IRELAND_PROVIDER_NAME, VERSION_1);
        TokenClaimsProducer tokenClaimsProducer = getTokenClaimsProducer(properties);
        AuthenticationService authenticationService = getAuthenticationService(properties, clock, tokenClaimsProducer);
        HttpClientFactory httpClientFactory = new DefaultHttpClientFactory(properties, registry, objectMapper);
        PaymentRequestSigner requestSigner = new BankOfIrelandPaymentRequestSigner(objectMapper, SIGNING_ALGORITHM);
        BankOfIrelandAuthMeansMapper authMeansMapper = new BankOfIrelandAuthMeansMapper();

        DefaultPaymentExecutionContextAdapterFactoryV2 paymentExecutionContextAdapterFactory = new DefaultPaymentExecutionContextAdapterFactoryV2(
                providerIdentification,
                objectMapper,
                authMeansMapper.getAuthMeansMapper(BANK_OF_IRELAND_PROVIDER_KEY),
                authenticationService,
                httpClientFactory,
                createSingleDataInitiationMapper(clock),
                null, //scheduled payments for now will nowt be enabled
                OBRisk1.PaymentContextCodeEnum.PARTYTOPARTY,
                () -> ENDPOINT_VERSION,
                requestSigner,
                clock,
                TokenScope.builder()
                        .grantScope(OpenBankingTokenScope.PAYMENTS.getGrantScope())
                        .authorizationUrlScope(OpenBankingTokenScope.PAYMENTS.getAuthorizationUrlScope())
                        .build(),
                new GenericConsentResponseStatusMapper(),
                new GenericStatusResponseStatusMapper(),
                new GenericScheduledConsentResponseStatusMapper(),
                new GenericScheduledResponseStatusMapper());

        return new GenericBasePaymentProviderV3(
                paymentExecutionContextAdapterFactory.createInitiatePaymentExecutionContextAdapter(),
                paymentExecutionContextAdapterFactory.createInitiateScheduledPaymentExecutionContextAdapter(),
                paymentExecutionContextAdapterFactory.createSubmitPaymentExecutionContextAdapter(),
                paymentExecutionContextAdapterFactory.createSubmitScheduledPaymentExecutionContextAdapter(),
                paymentExecutionContextAdapterFactory.createStatusPaymentExecutionContextAdapter(),
                paymentExecutionContextAdapterFactory.createStatusScheduledPaymentExecutionContextAdapter(),
                providerIdentification,
                () -> authMeansMapper.getTypedAuthMeans(),
                () -> HsmUtils.getKeyRequirements(SIGNING_PRIVATE_KEY_ID_NAME),
                () -> HsmUtils.getKeyRequirements(TRANSPORT_PRIVATE_KEY_ID_NAME, TRANSPORT_CERTIFICATE_NAME),
                ConsentValidityRules.EMPTY_RULES_SET,
                new UkProviderStateDeserializer(objectMapper));
    }

    private AuthenticationService getAuthenticationService(DefaultProperties properties, Clock clock, TokenClaimsProducer tokenClaimsProducer) {
        return new BankOfIrelandGroupAuthenticationService(properties.getOAuthAuthorizationUrl(),
                new DefaultMutualTlsOauth2Client(properties, new BankOfIrelandOauthTokenBodyProducer(), true),
                new ExternalUserRequestTokenSigner(SIGNING_ALGORITHM),
                tokenClaimsProducer,
                clock);
    }

    private TokenClaimsProducer getTokenClaimsProducer(DefaultProperties properties) {
        return new DefaultTokenClaimsProducer(new ExpiringJwtClaimsProducerDecorator(
                new DefaultJwtClaimsProducer(DefaultAuthMeans::getClientId, properties.getAudience()), 60));
    }

    private static PaymentDataInitiationMapper createSingleDataInitiationMapper(Clock clock) {
        return new PaymentDataInitiationMapper(
                new DateTimeAndUUIDBasedInstructionIdentificationSupplier(clock),
                new DefaultAmountFormatter(),
                new CamelCaseUkSchemeMapper())
                .validateAfterMapWith(new BankOfIrelandGroupPaymentRequestValidator())
                .withDebtorAccount();
    }
}
