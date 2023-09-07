package com.yolt.providers.abnamrogroup.common.pis;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.RestTemplateManagerConfiguration;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentStatuses;
import com.yolt.providers.common.pis.sepa.SepaPaymentStatusResponseDTO;
import com.yolt.providers.common.pis.sepa.SubmitPaymentRequest;
import com.yolt.providers.common.pis.sepa.SubmitPaymentRequestBuilder;
import com.yolt.providers.common.providerinterface.SepaPaymentProvider;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

/**
 * This test contains flow for scenarios when 500 error
 * occurs during payment submit process
 *
 * According to the doc: https://developer.abnamro.com/api-products/payment-initiation-psd2/reference-documentation#section/Overview/The-consent-application
 * when 500 error is returned for PUT operation, we should check the status of the payment using GET.
 * If status is still 'AUTHORIZED' we can try to make PUT call again.
 * If status changes to 'INPROGRESS', 'SCHEDULED' or 'EXECUTED' then execution succeeded.
 * If status is 'REJECTED', then contact the bank.
 * If PUT operation fails and we cannot check status, then contact bank.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(port = 0, httpsPort = 0, stubs = "classpath:/wiremock/api_1_2_1/pis/execute_payment_500")
@ActiveProfiles("test")
public class AbnAmroSepaPaymentProviderSubmitPayment500IntegrationTest {

    @Autowired
    private SepaPaymentProvider sut;

    @Mock
    private RestTemplateManager restTemplateManager;

    @Autowired
    private RestTemplateBuilder restTemplateBuilder;

    @Value("${wiremock.server.port}")
    private int port;

    @Test
    public void shouldReturnSepaPaymentStatusResponseDTOWithEmptyPaymentIdAndPecMetadataWithProperStatusesForSubmitPaymentWhen500Error() {
        // given
        AbnAmroTestPisAuthenticationMeans testPisAuthenticationMeans = new AbnAmroTestPisAuthenticationMeans();
        SubmitPaymentRequest submitPaymentRequest = new SubmitPaymentRequestBuilder()
                .setAuthenticationMeans(testPisAuthenticationMeans.getAuthMeans())
                .setProviderState("""
                        {"transactionId":"8325P3346070108S0PD","redirectUri":"https://www.yolt.com/callback/payment"}""")
                .setRestTemplateManager(restTemplateManager)
                .setRedirectUrlPostedBackFromSite("https://www.yolt.com/callback/payment?code=9C6UrsGZ0Z3XJymRAOAgl7hKPLlWKUo9GBfMQQEs")
                .build();

        given(restTemplateManager.manage(any(RestTemplateManagerConfiguration.class)))
                .willReturn(restTemplateBuilder
                        .rootUri("http://localhost:" + port)
                        .build());

        // when
        SepaPaymentStatusResponseDTO result = sut.submitPayment(submitPaymentRequest);

        // then
        assertThat(result.getPaymentId()).isEmpty();
        assertThat(result.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).extracting(statuses -> statuses.getRawBankPaymentStatus().getStatus(),
                        statuses -> statuses.getRawBankPaymentStatus().getReason(),
                        PaymentStatuses::getPaymentStatus)
                        .contains("MESSAGE_BAI560_0005",
                                "A technical error has occurred. Try again\nlater. Report the following code when \ncontacting the [Corporate API Services Team](corporate.api.services@nl.abnamro.com): MESSAGE_BAI560_0005",
                                EnhancedPaymentStatus.UNKNOWN));
    }
}
