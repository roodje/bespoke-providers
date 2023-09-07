package com.yolt.providers.abnamrogroup.common.pis;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.RestTemplateManagerConfiguration;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentStatuses;
import com.yolt.providers.common.pis.sepa.*;
import com.yolt.providers.common.providerinterface.SepaPaymentProvider;
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
 * This test contains flow for scenarios when 500 error
 * occurs during payment initiation process
 *
 * According to the doc: https://developer.abnamro.com/api-products/payment-initiation-psd2/reference-documentation#section/Overview/The-consent-application
 * when 500 error is returned for POST operation, then payment can be safely posted again.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(port = 0, httpsPort = 0, stubs = "classpath:/wiremock/api_1_2_1/pis/initiate_payment_500")
@ActiveProfiles("test")
public class AbnAmroSepaPaymentProviderInitiatePayment500IntegrationTest {

    @Autowired
    private SepaPaymentProvider sut;

    @Mock
    private RestTemplateManager restTemplateManager;

    @Autowired
    private RestTemplateBuilder restTemplateBuilder;

    @Value("${wiremock.server.port}")
    private int port;

    @Test
    public void shouldReturnLoginUrlAndStateDTOWithEmptyLoginUrlAndEmptyStateAndPecMetadataWithProperStatusesForInitiatePaymentWhenInternalServerError() {
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
                        .contains("MESSAGE_BAI560_0005",
                                "A technical error has occurred. Try again\nlater. Report the following code when \ncontacting the [Corporate API Services Team](corporate.api.services@nl.abnamro.com): MESSAGE_BAI560_0005",
                                EnhancedPaymentStatus.INITIATION_ERROR));
    }
}
