package com.yolt.providers.fineco.pis.happyFlow;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.FakeRestTemplateManager;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentStatuses;
import com.yolt.providers.common.pis.sepa.*;
import com.yolt.providers.common.rest.ExternalRestTemplateBuilderFactory;
import com.yolt.providers.fineco.FinecoPaymentProvider;
import com.yolt.providers.fineco.FinecoSampleTypedAuthenticationMeans;
import com.yolt.providers.fineco.FinecoTestApp;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This test contains all happy flows occurring in Fineco payment provider.
 * Covered flows:
 * - payment initiation
 * - payment status
 * <p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = FinecoTestApp.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(
        stubs = "classpath:/mappings/pis/v1/happy_flow",
        files = "classpath:/mappings/pis/v1/happy_flow",
        httpsPort = 0, port = 0)
@ActiveProfiles("fineco")
public class FinecoPaymentProviderHappyFlowIntegrationTests {

    private static final String STATUS_PAYMENT_ID = "StatusPaymentId";
    private static final String SUBMIT_PAYMENT_ID = "SubmitPaymentId";

    private Map<String, BasicAuthenticationMean> authenticationMeans = new FinecoSampleTypedAuthenticationMeans().getAuthenticationMeans();

    @Autowired
    private ExternalRestTemplateBuilderFactory externalRestTemplateBuilderFactory;

    @Autowired
    @Qualifier("FinecoObjectMapper")
    private ObjectMapper mapper;

    @Mock
    private Signer signer;

    private RestTemplateManager restTemplateManager;

    @Autowired
    private FinecoPaymentProvider paymentProvider;

    @BeforeEach
    public void setup() {
        this.restTemplateManager = new FakeRestTemplateManager(externalRestTemplateBuilderFactory);
    }

    @Test
    public void shouldInitiatePayment() {
        //given
        SepaInitiatePaymentRequestDTO sepaInitiatePaymentRequestDTO = SepaInitiatePaymentRequestDTO.builder()
                .debtorAccount(SepaAccountDTO.builder()
                        .currency(CurrencyCode.EUR)
                        .iban("IT31X0301503200000003517230")
                        .build())
                .creditorAccount(SepaAccountDTO.builder()
                        .currency(CurrencyCode.EUR)
                        .iban("DE02100100109307118603")
                        .build())
                .creditorName("Walter Bianchi")
                .instructedAmount(SepaAmountDTO.builder()
                        .amount(new BigDecimal("123.5"))
                        .build())
                .remittanceInformationUnstructured("causale pagamento")
                .build();

        InitiatePaymentRequest initiatePaymentRequest = new InitiatePaymentRequestBuilder()
                .setRestTemplateManager(restTemplateManager)
                .setAuthenticationMeans(authenticationMeans)
                .setState("fakeState")
                .setSigner(signer)
                .setPsuIpAddress("127.0.0.1")
                .setBaseClientRedirectUrl("https://www.yolt.com/callback/payment")
                .setRequestDTO(sepaInitiatePaymentRequestDTO)
                .build();

        //when
        LoginUrlAndStateDTO response = paymentProvider.initiatePayment(initiatePaymentRequest);

        //then

        assertThat(response.getLoginUrl()).isEqualTo("https://www.finecobank.com/1234-wertiq-983");
        assertThat(response.getProviderState()).isEqualTo("{\"paymentId\":\"1234-wertiq-983\",\"paymentType\":\"SINGLE\"}");
    }

    @Test
    public void shouldSuccessfullySubmitPayment() {
        // given
        SubmitPaymentRequest submitPaymentRequest = new SubmitPaymentRequestBuilder()
                .setRedirectUrlPostedBackFromSite("https://www.yolt.com/callback/payment?code=123456789&state=state") //not know what will be the int the url
                .setRestTemplateManager(restTemplateManager)
                .setProviderState(SUBMIT_PAYMENT_ID)
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(signer)
                .build();
        SepaPaymentStatusResponseDTO expectedResponse = SepaPaymentStatusResponseDTO.builder()
                .paymentId(SUBMIT_PAYMENT_ID)
                .build();

        // when
        SepaPaymentStatusResponseDTO response = paymentProvider.submitPayment(submitPaymentRequest);

        // then
        assertThat(response).isEqualTo(expectedResponse);
    }

    @Test
    public void shouldSuccessfullyGetPaymentStatus() {
        // given
        GetStatusRequest getStatusRequest = new GetStatusRequestBuilder()
                .setPaymentId(STATUS_PAYMENT_ID)
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManager)
                .build();

        // when
        SepaPaymentStatusResponseDTO response = paymentProvider.getStatus(getStatusRequest);

        // then
        assertThat(response.getPaymentId()).isEqualTo(STATUS_PAYMENT_ID);
        assertThat(response.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).extracting(statuses -> statuses.getRawBankPaymentStatus().getStatus(),
                                statuses -> statuses.getRawBankPaymentStatus().getReason(),
                                PaymentStatuses::getPaymentStatus)
                        .contains("ACCP", "", EnhancedPaymentStatus.ACCEPTED));
    }
}
