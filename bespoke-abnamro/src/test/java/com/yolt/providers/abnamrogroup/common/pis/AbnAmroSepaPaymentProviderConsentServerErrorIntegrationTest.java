package com.yolt.providers.abnamrogroup.common.pis;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.exception.MissingDataException;
import com.yolt.providers.common.pis.paymentexecutioncontext.exception.PaymentExecutionTechnicalException;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.sepa.SepaPaymentStatusResponseDTO;
import com.yolt.providers.common.pis.sepa.SubmitPaymentRequest;
import com.yolt.providers.common.pis.sepa.SubmitPaymentRequestBuilder;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * This test contains flow for 'server_error' error returned in redirectUrlPostedBackFromSite
 * during consent flow.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
public class AbnAmroSepaPaymentProviderConsentServerErrorIntegrationTest {

    @Autowired
    private AbnAmroPaymentProvider sut;

    @Mock
    private RestTemplateManager restTemplateManager;

    @Test
    public void shouldReturnResponseWithNoConsentFromUserStatusForSubmitPaymentWhenErrorInResponseParameters() {
        // given
        AbnAmroTestPisAuthenticationMeans testPisAuthenticationMeans = new AbnAmroTestPisAuthenticationMeans();
        String providerState = """
                {"accountNumber": "NL12ABNA9999876523", "transactionId": "8325P3346070108S0PD", "status": "STORED", "accountHolderName": "John Doe"}""";
        SubmitPaymentRequest submitPaymentRequest = new SubmitPaymentRequestBuilder()
                .setAuthenticationMeans(testPisAuthenticationMeans.getAuthMeans())
                .setProviderState(providerState)
                .setRestTemplateManager(restTemplateManager)
                .setRedirectUrlPostedBackFromSite("https://www.yolt.com/callback/payment?error=access_denied")
                .build();

        // when
        SepaPaymentStatusResponseDTO result = sut.submitPayment(submitPaymentRequest);

        // then
        assertThat(result.getPaymentId()).isEmpty();
        assertThat(result.getProviderState()).isEqualTo(providerState);
        assertThat(result.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("access_denied");
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.NO_CONSENT_FROM_USER);
                }));
    }

    @Test
    public void shouldThrowPaymentExecutionTechnicalExceptionForSubmitPaymentWhenNoErrorAndNoCodeInResponseParameters() {
        // given
        AbnAmroTestPisAuthenticationMeans testPisAuthenticationMeans = new AbnAmroTestPisAuthenticationMeans();
        String providerState = """
                {"accountNumber": "NL12ABNA9999876523", "transactionId": "8325P3346070108S0PD", "status": "STORED", "accountHolderName": "John Doe"}""";
        SubmitPaymentRequest submitPaymentRequest = new SubmitPaymentRequestBuilder()
                .setAuthenticationMeans(testPisAuthenticationMeans.getAuthMeans())
                .setProviderState(providerState)
                .setRestTemplateManager(restTemplateManager)
                .setRedirectUrlPostedBackFromSite("https://www.yolt.com/callback/payment?other=something")
                .build();

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> sut.submitPayment(submitPaymentRequest);

        // then
        assertThatExceptionOfType(PaymentExecutionTechnicalException.class)
                .isThrownBy(throwingCallable)
                .withMessage("submit_preparation_error")
                .withCauseInstanceOf(MissingDataException.class)
                .satisfies(ex -> assertThat(ex.getCause().getMessage()).isEqualTo("Missing data for key code."));
    }
}
