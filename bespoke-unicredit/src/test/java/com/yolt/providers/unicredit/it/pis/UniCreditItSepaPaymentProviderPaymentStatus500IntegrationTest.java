package com.yolt.providers.unicredit.it.pis;

import com.yolt.providers.FakeRestTemplateManager;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.exception.ConfirmationFailedException;
import com.yolt.providers.common.pis.sepa.SubmitPaymentRequest;
import com.yolt.providers.common.pis.sepa.SubmitPaymentRequestBuilder;
import com.yolt.providers.common.providerinterface.SepaPaymentProvider;
import com.yolt.providers.common.rest.ExternalRestTemplateBuilderFactory;
import com.yolt.providers.unicredit.TestApp;
import com.yolt.providers.unicredit.UnicreditSampleTypedAuthenticationMeans;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = TestApp.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/unicredit/it/pis/payment-status-500", httpsPort = 0, port = 0)
@ActiveProfiles("unicredit")
public class UniCreditItSepaPaymentProviderPaymentStatus500IntegrationTest {

    private static final String CERT_PATH = "certificates/unicredit/unicredit_certificate.pem";
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
    public void shouldThrowConfirmationFailedExceptionForSubmitPaymentWhenInternalServerErrorForGetStatusApiEndpoint() {
        // given
        SubmitPaymentRequest request = new SubmitPaymentRequestBuilder()
                .setAuthenticationMeans(testAuthenticationMeans.getAuthMeans())
                .setRestTemplateManager(restTemplateManager)
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .setRedirectUrlPostedBackFromSite("https://www.yolt.com/callback/payment?state=" + STATE)
                .setProviderState(PAYMENT_ID)
                .build();

        // when
        ThrowableAssert.ThrowingCallable getStatusCallable = () -> sepaPaymentProvider.submitPayment(request);

        // then
        assertThatThrownBy(getStatusCallable)
                .isInstanceOf(ConfirmationFailedException.class);
    }
}
