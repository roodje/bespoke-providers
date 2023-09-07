package com.yolt.providers.abnamrogroup.common.pis;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.RestTemplateManagerConfiguration;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentStatuses;
import com.yolt.providers.common.pis.sepa.*;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

/**
 * This test contains flow for scenarios when 4xx error
 * occurs during payment initiation process
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(port = 0, httpsPort = 0, stubs = "classpath:/wiremock/api_1_2_1/pis/initiate_payment_4xx")
@ActiveProfiles("test")
public class AbnAmroSepaPaymentProviderInitiatePayment4xxIntegrationTest {

    @Autowired
    private AbnAmroPaymentProvider sut;

    @Mock
    private RestTemplateManager restTemplateManager;

    @Autowired
    private RestTemplateBuilder restTemplateBuilder;

    @Value("${wiremock.server.port}")
    private int port;

    @Test
    public void shouldReturnLoginUrlAndStateDTOWithEmptyLoginUrlAndEmptyProviderStateWithProperStatusesInPecMetadataForInitiatePaymentWhen4xxError() {
        // given
        SepaInitiatePaymentRequestDTO requestDTO = SepaInitiatePaymentRequestDTO.builder()
                .debtorAccount(new SepaAccountDTO(CurrencyCode.EUR, ""))
                .creditorAccount(new SepaAccountDTO(CurrencyCode.EUR, "NL12ABNA9999876523"))
                .creditorName("John Doe")
                .instructedAmount(new SepaAmountDTO(new BigDecimal("149.99")))
                .remittanceInformationUnstructured("Payment of invoice 123/01")
                .build();
        String baseClientRedirectUrl = "https://www.yolt.com/callback/payment";
        AbnAmroTestPisAuthenticationMeans testPisAuthenticationMeans = new AbnAmroTestPisAuthenticationMeans();
        InitiatePaymentRequest initiatePaymentRequest = new InitiatePaymentRequestBuilder()
                .setRequestDTO(requestDTO)
                .setBaseClientRedirectUrl(baseClientRedirectUrl)
                .setRestTemplateManager(restTemplateManager)
                .setAuthenticationMeans(testPisAuthenticationMeans.getAuthMeans())
                .setState("state")
                .build();

        given(restTemplateManager.manage(any(RestTemplateManagerConfiguration.class)))
                .willReturn(restTemplateBuilder
                        .rootUri("http://localhost:" + port)
                        .build());

        // when
        LoginUrlAndStateDTO result = sut.initiatePayment(initiatePaymentRequest);

        // then
        assertThat(result.getProviderState()).isEmpty();
        assertThat(result.getLoginUrl()).isEmpty();
        assertThat(result.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).extracting(statuses -> statuses.getRawBankPaymentStatus().getStatus(),
                        statuses -> statuses.getRawBankPaymentStatus().getReason(),
                        PaymentStatuses::getPaymentStatus)
        .contains("ERR_1100_004", "An input paramater is invalid.", EnhancedPaymentStatus.INITIATION_ERROR));
    }
}
