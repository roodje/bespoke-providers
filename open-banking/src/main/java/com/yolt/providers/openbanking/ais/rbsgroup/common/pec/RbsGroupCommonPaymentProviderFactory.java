package com.yolt.providers.openbanking.ais.rbsgroup.common.pec;

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
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.amount.DefaultAmountFormatter;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.datainitiation.PaymentDataInitiationMapper;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.instructionidentification.DateTimeAndUUIDBasedInstructionIdentificationSupplier;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.scheme.CamelCaseUkSchemeMapper;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.status.GenericConsentResponseStatusMapper;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.status.GenericStatusResponseStatusMapper;
import com.yolt.providers.openbanking.ais.generic2.service.AuthenticationService;
import com.yolt.providers.openbanking.ais.rbsgroup.common.auth.RbsGroupAuthMeansBuilderV4;
import com.yolt.providers.openbanking.ais.rbsgroup.common.http.RbsGroupHttpClientFactoryV2;
import com.yolt.providers.openbanking.ais.rbsgroup.common.http.RbsGroupHttpPaymentRequestSignerV2;
import com.yolt.providers.openbanking.ais.rbsgroup.common.pec.mapper.validator.RbsGroupPaymentRequestValidator;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBRisk1;
import io.micrometer.core.instrument.MeterRegistry;
import org.jose4j.jws.AlgorithmIdentifiers;

import java.time.Clock;
import java.util.Map;
import java.util.function.Function;

import static com.yolt.providers.openbanking.ais.rbsgroup.common.auth.RbsGroupAuthMeansBuilderV4.*;

public class RbsGroupCommonPaymentProviderFactory {

    public static GenericBasePaymentProviderV2 createPaymentProvider(ProviderIdentification providerIdentification,
                                                                     AuthenticationService authenticationService,
                                                                     DefaultProperties properties,
                                                                     MeterRegistry registry,
                                                                     ObjectMapper objectMapper,
                                                                     EndpointsVersionable endpointsVersionable,
                                                                     Clock clock) {
        Function<Map<String, BasicAuthenticationMean>, DefaultAuthMeans> authMeansProvider =
                authenticationMeans -> RbsGroupAuthMeansBuilderV4
                        .createAuthenticationMeansForPis(providerIdentification.getDisplayName(), authenticationMeans);
        DateTimeAndUUIDBasedInstructionIdentificationSupplier instructionIdentificationSupplier = new DateTimeAndUUIDBasedInstructionIdentificationSupplier(clock);

        PaymentDataInitiationMapper reversedRemittanceInformationUkDomesticDataInitiationMapper = new PaymentDataInitiationMapper(instructionIdentificationSupplier,
                new DefaultAmountFormatter(),
                new CamelCaseUkSchemeMapper())
                .withDebtorAccount()
                .reversingRemittanceInformation()
                .validateAfterMapWith(new RbsGroupPaymentRequestValidator());

        PaymentDataInitiationMapper ukDomesticDataInitiationMapper = new PaymentDataInitiationMapper(instructionIdentificationSupplier,
                new DefaultAmountFormatter(),
                new CamelCaseUkSchemeMapper())
                .withDebtorAccount()
                .validateAfterMapWith(new RbsGroupPaymentRequestValidator());

        TokenScope tokenScope = TokenScope.builder()
                .grantScope(OpenBankingTokenScope.PAYMENTS.getGrantScope())
                .authorizationUrlScope(OpenBankingTokenScope.PAYMENTS.getAuthorizationUrlScope())
                .build();
        DefaultPaymentExecutionContextAdapterFactory executionContextAdapterFactory = new DefaultPaymentExecutionContextAdapterFactory(
                providerIdentification,
                objectMapper,
                authMeansProvider,
                authenticationService,
                new RbsGroupHttpClientFactoryV2(properties, registry, objectMapper),
                ukDomesticDataInitiationMapper,
                OBRisk1.PaymentContextCodeEnum.PARTYTOPARTY,
                endpointsVersionable,
                new RbsGroupHttpPaymentRequestSignerV2(objectMapper, AlgorithmIdentifiers.RSA_PSS_USING_SHA256),
                clock,
                tokenScope,
                new GenericConsentResponseStatusMapper(),
                new GenericStatusResponseStatusMapper()
        );

        return new GenericBasePaymentProviderV2(executionContextAdapterFactory.createInitiatePaymentExecutionContextAdapter(),
                executionContextAdapterFactory.createSubmitPaymentExecutionContextAdapter(),
                executionContextAdapterFactory.createStatusPaymentExecutionContextAdapter(),
                providerIdentification,
                getTypedAuthenticationMeans(),
                () -> HsmUtils.getKeyRequirements(SIGNING_PRIVATE_KEY_ID_NAME),
                () -> HsmUtils.getKeyRequirements(TRANSPORT_PRIVATE_KEY_ID_NAME),
                ConsentValidityRules.EMPTY_RULES_SET);
    }
}
