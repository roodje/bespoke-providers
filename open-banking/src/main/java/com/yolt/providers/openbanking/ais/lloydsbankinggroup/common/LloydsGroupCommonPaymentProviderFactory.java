package com.yolt.providers.openbanking.ais.lloydsbankinggroup.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.openbanking.ais.common.HsmUtils;
import com.yolt.providers.openbanking.ais.generic2.DefaultProperties;
import com.yolt.providers.openbanking.ais.generic2.GenericBasePaymentProviderV2;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.common.EndpointsVersionable;
import com.yolt.providers.openbanking.ais.generic2.common.OpenBankingTokenScope;
import com.yolt.providers.openbanking.ais.generic2.common.ProviderIdentification;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import com.yolt.providers.openbanking.ais.generic2.pec.DefaultPaymentExecutionContextAdapterFactory;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.PaymentRequestAdjuster;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.amount.DefaultAmountFormatter;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.datainitiation.PaymentDataInitiationMapper;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.instructionidentification.DateTimeAndUUIDBasedInstructionIdentificationSupplier;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.scheme.CamelCaseUkSchemeMapper;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.status.GenericConsentResponseStatusMapper;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.status.GenericStatusResponseStatusMapper;
import com.yolt.providers.openbanking.ais.generic2.service.AuthenticationService;
import com.yolt.providers.openbanking.ais.generic2.signer.ExternalPaymentNoB64RequestSigner;
import com.yolt.providers.openbanking.ais.lloydsbankinggroup.common.auth.LloydsBankingGroupAuthenticationMeansV3;
import com.yolt.providers.openbanking.ais.lloydsbankinggroup.common.http.LloydsBankingGroupHttpClientFactoryV2;
import com.yolt.providers.openbanking.ais.lloydsbankinggroup.common.pec.common.LloydsBankingGroupPaymentHttpHeadersFactory;
import com.yolt.providers.openbanking.ais.lloydsbankinggroup.common.pec.common.LloydsBankingGroupPaymentRequestKeyProvider;
import com.yolt.providers.openbanking.ais.lloydsbankinggroup.common.pec.common.mapper.validator.LloydsBankingGroupPaymentRequestValidator;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBRisk1;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomestic2DataInitiation;
import io.micrometer.core.instrument.MeterRegistry;
import org.jose4j.jws.AlgorithmIdentifiers;

import java.time.Clock;
import java.util.Map;
import java.util.function.Function;

import static com.yolt.providers.openbanking.ais.lloydsbankinggroup.common.auth.LloydsBankingGroupAuthenticationMeansV3.*;

public class LloydsGroupCommonPaymentProviderFactory {

    public static GenericBasePaymentProviderV2 createPaymentProvider(ProviderIdentification providerIdentification,
                                                                     AuthenticationService authenticationService,
                                                                     DefaultProperties properties,
                                                                     MeterRegistry registry,
                                                                     ObjectMapper objectMapper,
                                                                     EndpointsVersionable endpointsVersionable,
                                                                     Clock clock,
                                                                     PaymentRequestAdjuster<OBWriteDomestic2DataInitiation> adjuster) {
        Function<Map<String, BasicAuthenticationMean>, DefaultAuthMeans> authMeansProvider =
                authenticationMeans -> LloydsBankingGroupAuthenticationMeansV3.createAuthenticationMeansForPis(authenticationMeans, providerIdentification.getIdentifier());
        DateTimeAndUUIDBasedInstructionIdentificationSupplier instructionIdentificationSupplier = new DateTimeAndUUIDBasedInstructionIdentificationSupplier(clock);
        PaymentDataInitiationMapper ukDomesticDataInitiationMapper = new PaymentDataInitiationMapper(instructionIdentificationSupplier,
                new DefaultAmountFormatter(),
                new CamelCaseUkSchemeMapper())
                .withDebtorAccount()
                .withAdjuster(adjuster)
                .validateAfterMapWith(new LloydsBankingGroupPaymentRequestValidator());
        TokenScope tokenScope = TokenScope.builder()
                .grantScope(OpenBankingTokenScope.PAYMENTS.getGrantScope())
                .authorizationUrlScope(OpenBankingTokenScope.PAYMENTS.getAuthorizationUrlScope())
                .build();
        DefaultPaymentExecutionContextAdapterFactory executionContextAdapterFactory = new DefaultPaymentExecutionContextAdapterFactory(
                providerIdentification,
                objectMapper,
                authMeansProvider,
                authenticationService,
                new LloydsBankingGroupHttpClientFactoryV2(properties, registry, objectMapper),
                ukDomesticDataInitiationMapper,
                OBRisk1.PaymentContextCodeEnum.PARTYTOPARTY,
                endpointsVersionable,
                new LloydsBankingGroupPaymentHttpHeadersFactory(new ExternalPaymentNoB64RequestSigner(objectMapper, AlgorithmIdentifiers.RSA_PSS_USING_SHA256),
                        new LloydsBankingGroupPaymentRequestKeyProvider(objectMapper)),
                clock,
                tokenScope,
                new GenericConsentResponseStatusMapper(),
                new GenericStatusResponseStatusMapper()
        );

        return new GenericBasePaymentProviderV2(executionContextAdapterFactory.createInitiatePaymentExecutionContextAdapter(),
                executionContextAdapterFactory.createSubmitPaymentExecutionContextAdapter(),
                executionContextAdapterFactory.createStatusPaymentExecutionContextAdapter(),
                providerIdentification,
                LloydsBankingGroupAuthenticationMeansV3.getTypedAuthenticationMeansForPis(),
                () -> HsmUtils.getKeyRequirements(SIGNING_PRIVATE_KEY_ID_NAME),
                () -> HsmUtils.getKeyRequirements(TRANSPORT_PRIVATE_KEY_ID_NAME, TRANSPORT_CERTIFICATE_NAME),
                ConsentValidityRules.EMPTY_RULES_SET);
    }
}
