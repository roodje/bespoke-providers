package com.yolt.providers.openbanking.ais.hsbcgroup.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.openbanking.ais.common.HsmUtils;
import com.yolt.providers.openbanking.ais.generic2.GenericBasePaymentProviderV3;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.common.EndpointsVersionable;
import com.yolt.providers.openbanking.ais.generic2.common.OpenBankingTokenScope;
import com.yolt.providers.openbanking.ais.generic2.common.ProviderIdentification;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import com.yolt.providers.openbanking.ais.generic2.http.DefaultHttpClientFactory;
import com.yolt.providers.openbanking.ais.generic2.pec.DefaultPaymentExecutionContextAdapterFactoryV2;
import com.yolt.providers.openbanking.ais.generic2.pec.common.UkProviderStateDeserializer;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.amount.DefaultAmountFormatter;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.datainitiation.PaymentDataInitiationMapper;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.datainitiation.ScheduledPaymentDataInitiationMapper;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.instructionidentification.DateTimeAndUUIDBasedInstructionIdentificationSupplier;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.scheme.CamelCaseUkSchemeMapper;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.status.GenericConsentResponseStatusMapper;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.status.GenericScheduledConsentResponseStatusMapper;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.status.GenericScheduledResponseStatusMapper;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.status.GenericStatusResponseStatusMapper;
import com.yolt.providers.openbanking.ais.generic2.service.AuthenticationService;
import com.yolt.providers.openbanking.ais.generic2.signer.ExternalPaymentRequestSigner;
import com.yolt.providers.openbanking.ais.hsbcgroup.common.auth.HsbcGroupAuthMeansBuilderV3;
import com.yolt.providers.openbanking.ais.hsbcgroup.common.auth.HsbcGroupPisTypedAuthMeansSupplier;
import com.yolt.providers.openbanking.ais.hsbcgroup.common.service.pis.HsbcGroupPaymentRequestValidator;
import com.yolt.providers.openbanking.ais.hsbcgroup.common.service.pis.HsbcGroupScheduledPaymentRequestValidator;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBRisk1;

import java.time.Clock;
import java.util.Map;
import java.util.function.Function;

import static com.yolt.providers.openbanking.ais.hsbcgroup.common.auth.HsbcGroupAuthMeansBuilderV3.*;

public class HsbcGroupCommonPaymentProviderFactoryV2 {

    public static GenericBasePaymentProviderV3 createPaymentProvider(ProviderIdentification providerIdentification,
                                                                     AuthenticationService authenticationService,
                                                                     DefaultHttpClientFactory httpClientFactory,
                                                                     ExternalPaymentRequestSigner payloadSigner,
                                                                     ObjectMapper objectMapper,
                                                                     EndpointsVersionable endpointsVersionable,
                                                                     Clock clock) {
        Function<Map<String, BasicAuthenticationMean>, DefaultAuthMeans> authMeansProvider = authenticationMeans ->
                HsbcGroupAuthMeansBuilderV3.createAuthenticationMeansForPis(authenticationMeans, providerIdentification.getDisplayName());
        TokenScope tokenScope = TokenScope.builder()
                .grantScope(OpenBankingTokenScope.PAYMENTS.getGrantScope())
                .authorizationUrlScope(OpenBankingTokenScope.PAYMENTS.getAuthorizationUrlScope())
                .build();
        DefaultPaymentExecutionContextAdapterFactoryV2 paymentExecutionContextAdapterFactory = new DefaultPaymentExecutionContextAdapterFactoryV2(
                providerIdentification,
                objectMapper,
                authMeansProvider,
                authenticationService,
                httpClientFactory,
                createSingleDataInitiationMapper(clock),
                createScheduledDataInitiationMapper(clock),
                OBRisk1.PaymentContextCodeEnum.PARTYTOPARTY,
                endpointsVersionable,
                payloadSigner,
                clock,
                tokenScope,
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
                new HsbcGroupPisTypedAuthMeansSupplier(),
                () -> HsmUtils.getKeyRequirements(SIGNING_PRIVATE_KEY_ID_NAME),
                () -> HsmUtils.getKeyRequirements(TRANSPORT_PRIVATE_KEY_ID_NAME, TRANSPORT_CERTIFICATE_NAME),
                ConsentValidityRules.EMPTY_RULES_SET,
                new UkProviderStateDeserializer(objectMapper));
    }

    private static PaymentDataInitiationMapper createSingleDataInitiationMapper(Clock clock) {
        return new PaymentDataInitiationMapper(
                new DateTimeAndUUIDBasedInstructionIdentificationSupplier(clock),
                new DefaultAmountFormatter(),
                new CamelCaseUkSchemeMapper())
                .validateAfterMapWith(new HsbcGroupPaymentRequestValidator())
                .withDebtorAccount();
    }

    private static ScheduledPaymentDataInitiationMapper createScheduledDataInitiationMapper(Clock clock) {
        return new ScheduledPaymentDataInitiationMapper(
                new DateTimeAndUUIDBasedInstructionIdentificationSupplier(clock),
                new DefaultAmountFormatter(),
                new CamelCaseUkSchemeMapper())
                .validateAfterMappingWith(new HsbcGroupScheduledPaymentRequestValidator(clock))
                .withDebtorAccount();
    }
}
