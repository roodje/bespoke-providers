package com.yolt.providers.openbanking.ais.tsbgroup.common;

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
import com.yolt.providers.openbanking.ais.generic2.http.DefaultHttpClientFactory;
import com.yolt.providers.openbanking.ais.generic2.pec.DefaultPaymentExecutionContextAdapterFactory;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.amount.DefaultAmountFormatter;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.datainitiation.PaymentDataInitiationMapper;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.instructionidentification.DateTimeAndUUIDBasedInstructionIdentificationSupplier;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.scheme.CamelCaseUkSchemeMapper;
import com.yolt.providers.openbanking.ais.generic2.service.AuthenticationService;
import com.yolt.providers.openbanking.ais.generic2.signer.ExternalPaymentRequestSigner;
import com.yolt.providers.openbanking.ais.tsbgroup.common.auth.TsbGroupAuthMeansBuilderV3;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBRisk1;
import io.micrometer.core.instrument.MeterRegistry;
import org.jose4j.jws.AlgorithmIdentifiers;

import java.time.Clock;
import java.util.Map;
import java.util.function.Function;

public class TsbGroupCommonPaymentProviderFactory {

    public static GenericBasePaymentProviderV2 createPaymentProvider(ProviderIdentification providerIdentification,
                                                                     AuthenticationService authenticationService,
                                                                     DefaultProperties properties,
                                                                     MeterRegistry registry,
                                                                     ObjectMapper objectMapper,
                                                                     EndpointsVersionable endpointsVersionable,
                                                                     Clock clock) {
        Function<Map<String, BasicAuthenticationMean>, DefaultAuthMeans> authMeansProvider =
                authenticationMeans -> TsbGroupAuthMeansBuilderV3.createAuthenticationMeans(authenticationMeans, providerIdentification.getIdentifier());
        DateTimeAndUUIDBasedInstructionIdentificationSupplier instructionIdentificationSupplier = new DateTimeAndUUIDBasedInstructionIdentificationSupplier(clock);
        PaymentDataInitiationMapper ukDomesticDataInitiationMapper = new PaymentDataInitiationMapper(instructionIdentificationSupplier,
                new DefaultAmountFormatter(),
                new CamelCaseUkSchemeMapper());

        TokenScope tokenScope = TokenScope.builder()
                .grantScope(OpenBankingTokenScope.PAYMENTS.getGrantScope())
                .authorizationUrlScope(OpenBankingTokenScope.PAYMENTS.getAuthorizationUrlScope())
                .build();
        DefaultPaymentExecutionContextAdapterFactory executionContextAdapterFactory = new TsbPaymentExecutionContextAdapterFactory(
                providerIdentification,
                objectMapper,
                authMeansProvider,
                authenticationService,
                new DefaultHttpClientFactory(properties, registry, objectMapper),
                ukDomesticDataInitiationMapper,
                OBRisk1.PaymentContextCodeEnum.PARTYTOPARTY,
                endpointsVersionable,
                new ExternalPaymentRequestSigner(objectMapper, AlgorithmIdentifiers.RSA_PSS_USING_SHA256),
                clock,
                tokenScope
        );

        return new GenericBasePaymentProviderV2(executionContextAdapterFactory.createInitiatePaymentExecutionContextAdapter(),
                executionContextAdapterFactory.createSubmitPaymentExecutionContextAdapter(),
                executionContextAdapterFactory.createStatusPaymentExecutionContextAdapter(),
                providerIdentification,
                TsbGroupAuthMeansBuilderV3.getTypedAuthenticationMeans(),
                () -> HsmUtils.getKeyRequirements(TsbGroupAuthMeansBuilderV3.SIGNING_PRIVATE_KEY_ID_NAME),
                () -> HsmUtils.getKeyRequirements(TsbGroupAuthMeansBuilderV3.TRANSPORT_PRIVATE_KEY_ID_NAME, TsbGroupAuthMeansBuilderV3.TRANSPORT_CERTIFICATE_NAME),
                ConsentValidityRules.EMPTY_RULES_SET);
    }
}
