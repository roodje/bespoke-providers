package com.yolt.providers.openbanking.ais.monzogroup;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.pis.common.GetStatusRequest;
import com.yolt.providers.common.pis.common.PaymentType;
import com.yolt.providers.common.pis.common.SubmitPaymentRequest;
import com.yolt.providers.common.pis.ukdomestic.*;
import com.yolt.providers.openbanking.ais.exception.UnexpectedJsonElementException;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.providershared.api.AuthenticationMeansReference;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;

import static com.yolt.providers.openbanking.ais.common.v4.ukpaymentmapper.WithoutDebtorUkPaymentMapper.REMITTANCE_REFERENCE_DYNAMIC_FIELD_NAME;

public class MonzoTestUtilV2 {

    private static final String TEST_PSU_IP_ADDRESS = "127.0.0.1";
    private static final String TEST_REDIRECT_URL = "https://www.yolt.com/callback-test";
    private static final String TEST_STATE = "aTestState";
    private static final String STUBBED_CONSENT_ID = "obpispdomesticpaymentconsent_00009mynmtLy5yvfOeJjqD";
    private static final String REDIRECT_URL = "https://www.yolt.com/callback/5fe1e9f8-eb5f-4812-a6a6-2002759db545";


    public static InitiateUkDomesticPaymentRequestDTO createSampleInitiateRequestDTO(AccountIdentifierScheme accountIdentifierScheme, String amount) {
        UkAccountDTO debtorAccount = new UkAccountDTO("8272908780568576", accountIdentifierScheme, "Alex Mitchell", null);
        UkAccountDTO creditorAccount = new UkAccountDTO("1802968485593088", accountIdentifierScheme, "Jordan Bell", null);
        return new InitiateUkDomesticPaymentRequestDTO(
                "35B64F93",
                CurrencyCode.GBP.toString(),
                new BigDecimal(amount),
                creditorAccount,
                debtorAccount,
                "Unstructured",
                Collections.singletonMap(REMITTANCE_REFERENCE_DYNAMIC_FIELD_NAME, "Structured")
        );
    }

    public static InitiateUkDomesticPaymentRequestDTO createValidInitiateRequestForUkDomesticPayment(AccountIdentifierScheme accountIdentifierScheme) {
        return createSampleInitiateRequestDTO(accountIdentifierScheme, "0.01");
    }

    public static SubmitPaymentRequest createConfirmPaymentRequestGivenProviderState(Map<String, BasicAuthenticationMean> authenticationMeans,
                                                                                     Signer signer,
                                                                                     RestTemplateManagerMock restTemplateManagerMock,
                                                                                     AuthenticationMeansReference authenticationMeansReference,
                                                                                     String providerState) {
        String authorizationCode = "sample_auth_code";
        return new SubmitPaymentRequest(
                providerState,
                authenticationMeans,
                "https://www.yolt.com/callback/payments?code=" + authorizationCode,
                signer,
                restTemplateManagerMock,
                TEST_PSU_IP_ADDRESS,
                authenticationMeansReference
        );
    }

    public static InitiateUkDomesticPaymentRequest createInitiateRequestDTO(Map<String, BasicAuthenticationMean> authenticationMeans,
                                                                            Signer signer,
                                                                            RestTemplateManagerMock restTemplateManagerMock,
                                                                            AuthenticationMeansReference authenticationMeansReference,
                                                                            InitiateUkDomesticPaymentRequestDTO requestDTO) {
        return new InitiateUkDomesticPaymentRequest(
                requestDTO,
                TEST_REDIRECT_URL,
                TEST_STATE,
                authenticationMeans,
                signer,
                restTemplateManagerMock,
                TEST_PSU_IP_ADDRESS,
                authenticationMeansReference
        );
    }

    public static GetStatusRequest createGetStatusRequest(Map<String, BasicAuthenticationMean> authenticationMeans,
                                                          Signer signer,
                                                          RestTemplateManager restTemplateManagerMock,
                                                          AuthenticationMeansReference authenticationMeansReference,
                                                          boolean withPaymentId) {
        return new GetStatusRequest(createUkProviderState(new UkProviderState("331d76df48ed41229b67f062dd55e340", PaymentType.SINGLE, null)),
                withPaymentId ? "e23f5d5cd08d44c3993243ad3f19d56e" : null,
                authenticationMeans,
                signer,
                restTemplateManagerMock,
                null,
                authenticationMeansReference);
    }

    private static String createUkProviderState(UkProviderState ukProviderState) {
        try {
            return new ObjectMapper().writeValueAsString(ukProviderState);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    public static String getSerializedAccessMeans(AccessMeans monzoGroupAccessMeans,
                                                  ObjectMapper objectMapper) {
        try {
            return objectMapper.writeValueAsString(monzoGroupAccessMeans);
        } catch (JsonProcessingException e) {
            throw new UnexpectedJsonElementException("Unable to serialize oAuthToken", e);
        }
    }
}
