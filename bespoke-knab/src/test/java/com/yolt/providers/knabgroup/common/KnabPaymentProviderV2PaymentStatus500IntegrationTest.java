package com.yolt.providers.knabgroup.common;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.sepa.GetStatusRequest;
import com.yolt.providers.common.pis.sepa.GetStatusRequestBuilder;
import com.yolt.providers.common.pis.sepa.SepaPaymentStatusResponseDTO;
import com.yolt.providers.common.providerinterface.SepaPaymentProvider;
import com.yolt.providers.common.rest.ExternalRestTemplateBuilderFactory;
import com.yolt.providers.knabgroup.TestRestTemplateManager;
import com.yolt.providers.knabgroup.TestSigner;
import com.yolt.providers.knabgroup.samples.SampleAuthenticationMeans;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;

import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This test contains flow for scenarios when 500 error
 * occurs during get payment status process
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/pis/status-payment-http500", httpsPort = 0, port = 0)
public class KnabPaymentProviderV2PaymentStatus500IntegrationTest {

    private TestRestTemplateManager restTemplateManager;
    private Signer signer;
    private Map<String, BasicAuthenticationMean> authenticationMeans;

    @Autowired
    private ExternalRestTemplateBuilderFactory externalRestTemplateBuilderFactory;

    @Autowired
    @Qualifier("KnabPaymentProvider")
    private KnabGroupPaymentProvider knabPaymentProvider;

    Stream<SepaPaymentProvider> getPaymentProviders() {
        return Stream.of(knabPaymentProvider);
    }

    @BeforeEach
    public void setup() {
        authenticationMeans = SampleAuthenticationMeans.getSampleAuthenticationMeans();
        restTemplateManager = new TestRestTemplateManager(externalRestTemplateBuilderFactory);
        signer = new TestSigner();
    }

    @ParameterizedTest
    @MethodSource("getPaymentProviders")
    public void shouldReturnSepaPaymentStatusResponseDTOWithEmptyPaymentIdAndPecMetadataWithProperStatusesForGetPaymentStatusWhen500Error(final SepaPaymentProvider subject) {
        // given
        GetStatusRequest getStatusRequest = new GetStatusRequestBuilder()
                .setPaymentId("c7b9461a2d324091ab4b83df8853bb92")
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManager)
                .setPsuIpAddress("127.0.0.1")
                .build();

        // when
        SepaPaymentStatusResponseDTO result = subject.getStatus(getStatusRequest);

        // then
        assertThat(result.getPaymentId()).isEmpty();
        assertThat(result.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("PAYMENT_FAILED");
                    assertThat(statuses.getRawBankPaymentStatus().getReason()).isEmpty();
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.UNKNOWN);
                }));
    }
}
