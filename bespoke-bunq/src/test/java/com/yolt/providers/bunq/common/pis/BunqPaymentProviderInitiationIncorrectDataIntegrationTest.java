package com.yolt.providers.bunq.common.pis;

import com.yolt.providers.bunq.AuthMeans;
import com.yolt.providers.bunq.RestTemplateManagerMock;
import com.yolt.providers.bunq.TestApp;
import com.yolt.providers.bunq.common.BunqPaymentProvider;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.pis.paymentexecutioncontext.exception.PaymentExecutionTechnicalException;
import com.yolt.providers.common.pis.sepa.*;
import com.yolt.providers.common.rest.ExternalRestTemplateBuilderFactory;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@SpringBootTest(classes = TestApp.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/mappings/pis", httpsPort = 0, port = 0)
public class BunqPaymentProviderInitiationIncorrectDataIntegrationTest {

    private static final Map<String, BasicAuthenticationMean> AUTHENTICATION_MEANS = AuthMeans.prepareAuthMeansV2();

    @Autowired
    private BunqPaymentProvider bunqPaymentProvider;

    @Autowired
    private ExternalRestTemplateBuilderFactory externalRestTemplateBuilderFactory;

    private RestTemplateManagerMock restTemplateManagerMock;

    @BeforeEach
    public void beforeEach() {
        restTemplateManagerMock = new RestTemplateManagerMock(externalRestTemplateBuilderFactory);
    }

    @Mock
    private Signer signer;

    @Test
    void shouldReturnPaymentExecutionTechnicalExceptionWhenIncorrectDataIsProvided() {
        // given
        SepaInitiatePaymentRequestDTO requestDTO = SepaInitiatePaymentRequestDTO.builder()
                .creditorAccount(new SepaAccountDTO(CurrencyCode.EUR, "NL91ABNA0417164300"))
                .debtorAccount(new SepaAccountDTO(CurrencyCode.EUR, ""))
                .creditorName("Some Name")
                .endToEndIdentification("123456789012345")
                .instructedAmount(new SepaAmountDTO(new BigDecimal("5877.78")))
                .remittanceInformationUnstructured("unstructured-information")
                .build();
        InitiatePaymentRequest initiatePaymentRequest = new InitiatePaymentRequestBuilder()
                .setRequestDTO(requestDTO)
                .setBaseClientRedirectUrl("https://www.yolt.com/callback/payment")
                .setAuthenticationMeans(AUTHENTICATION_MEANS)
                .setSigner(signer)
                .setState("someState")
                .setRestTemplateManager(restTemplateManagerMock)
                .setPsuIpAddress("111.1.111.111")
                .build();
        Exception expectedRootException = new IllegalArgumentException("Debtor account is required");

        //when
        ThrowableAssert.ThrowingCallable call = () -> bunqPaymentProvider.initiatePayment(initiatePaymentRequest);

        //then
        assertThatExceptionOfType(PaymentExecutionTechnicalException.class)
                .isThrownBy(call)
                .withMessage("request_creation_error")
                .withCause(expectedRootException);
    }
}

