package com.yolt.providers.unicredit.it.pis;

import com.yolt.providers.FakeRestTemplateManager;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.pis.sepa.*;
import com.yolt.providers.common.providerinterface.SepaPaymentProvider;
import com.yolt.providers.common.rest.ExternalRestTemplateBuilderFactory;
import com.yolt.providers.unicredit.TestApp;
import com.yolt.providers.unicredit.UnicreditSampleTypedAuthenticationMeans;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = TestApp.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/unicredit/it/pis/happy-flow", httpsPort = 0, port = 0)
@ActiveProfiles("unicredit")
public class UniCreditItSepaPaymentProviderHappyFlowIntegrationTest {

    private static final String CERT_PATH = "certificates/unicredit/unicredit_certificate.pem";
    private static final String REDIRECT_URL = "https://www.yolt.com/callback/payment";
    private static final String STATE = "8b6dee15-ea2a-49b2-b100-f5f96d31cd90";
    private static final String PSU_IP_ADDRESS = "192.160.1.2";
    public static final String PAYMENT_ID = "1234-wertiq-983";

    @Autowired
    private ExternalRestTemplateBuilderFactory externalRestTemplateBuilderFactory;

    private RestTemplateManager restTemplateManager;

    private UnicreditSampleTypedAuthenticationMeans testAuthenticationMeans;

    @Autowired
    @Qualifier("UniCreditItSepaPaymentProviderV1")
    private SepaPaymentProvider sepaPaymentProvider;

    @BeforeEach
    public void setup() throws Exception {
        testAuthenticationMeans = new UnicreditSampleTypedAuthenticationMeans(CERT_PATH);
        restTemplateManager = new FakeRestTemplateManager(externalRestTemplateBuilderFactory);
    }

    @Test
    public void shouldReturnResultWithPaymentIdInProviderStateAndLoginUrlForInitiatePaymentWithCorrectData() {
        // given
        InitiatePaymentRequest request = new InitiatePaymentRequestBuilder()
                .setAuthenticationMeans(testAuthenticationMeans.getAuthMeans())
                .setRestTemplateManager(restTemplateManager)
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .setBaseClientRedirectUrl(REDIRECT_URL)
                .setState(STATE)
                .setRequestDTO(SepaInitiatePaymentRequestDTO.builder()
                        .debtorAccount(SepaAccountDTO.builder()
                                .iban("IT18L0200811770000019486580")
                                .currency(CurrencyCode.EUR)
                                .build())
                        .instructedAmount(SepaAmountDTO.builder()
                                .amount(new BigDecimal("123.45"))
                                .build())
                        .creditorAccount(SepaAccountDTO.builder()
                                .iban("IT18L0200811770000019486581")
                                .currency(CurrencyCode.EUR)
                                .build())
                        .creditorName("John Kowalsky")
                        .remittanceInformationUnstructured("Payment for dinner")
                        .endToEndIdentification("asdfg123245")
                        .build())
                .build();

        // when
        LoginUrlAndStateDTO result = sepaPaymentProvider.initiatePayment(request);

        // then
        assertThat(result.getProviderState()).isEqualTo(PAYMENT_ID);
        assertThat(result.getLoginUrl()).isEqualTo("https://www.testbank.com/asdfasdfasdf");
    }

    @Test
    public void shouldReturnResponseWithPaymentIdAndStatusAcceptedForSubmitPaymentWithCorrectData() {
        // given
        SubmitPaymentRequest request = new SubmitPaymentRequestBuilder()
                .setAuthenticationMeans(testAuthenticationMeans.getAuthMeans())
                .setRestTemplateManager(restTemplateManager)
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .setRedirectUrlPostedBackFromSite("https://www.yolt.com/callback/payment?state=" + STATE)
                .setProviderState(PAYMENT_ID)
                .build();

        // when
        SepaPaymentStatusResponseDTO result = sepaPaymentProvider.submitPayment(request);

        // then
        assertThat(result.getPaymentId()).isEqualTo(PAYMENT_ID);
    }

    @Test
    public void shouldReturnResponseWithPaymentIdAndStatusAcceptedForGetStatusWithCorrectData() {
        // given
        GetStatusRequest request = new GetStatusRequestBuilder()
                .setPaymentId(PAYMENT_ID)
                .setAuthenticationMeans(testAuthenticationMeans.getAuthMeans())
                .setRestTemplateManager(restTemplateManager)
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .build();

        // when
        SepaPaymentStatusResponseDTO result = sepaPaymentProvider.getStatus(request);

        // then
        assertThat(result.getPaymentId()).isEqualTo(PAYMENT_ID);
    }
}
