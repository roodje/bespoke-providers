package com.yolt.providers.stet.boursoramagroup;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.ConfirmationFailedException;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.sepa.*;
import com.yolt.providers.common.providerinterface.SepaPaymentProvider;
import com.yolt.providers.stet.generic.GenericPaymentProviderV2;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import org.bouncycastle.util.encoders.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * This test contains flow for scenarios when 500 error
 * occurs during payment submission process - while getting payment status
 * In such case we assume payment is not properly confirmed and we throw {@link ConfirmationFailedException}.
 * Tests is parametrized and run for all providers in group
 */

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = BoursoramaGroupTestConfig.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("boursorama")
@AutoConfigureWireMock(stubs = {
        "classpath:/stubs/boursorama/pis/payment-status-500",
        "classpath:/stubs/boursorama/pis/happy-flow/token"}, httpsPort = 0, port = 0)
public class BoursoramaPaymentProviderV3PaymentConfirmation500IntegrationTest {

    private static final String PAYMENT_ID = "XyXmxIfId";
    private static final String REDIRECT_URL_POSTED_BACK_FROM_SITE = "https://clients.boursorama.com/finalisation-virement/" + PAYMENT_ID;
    private static final String PSU_IP_ADDRESS = "127.0.0.1";
    private static final String SERIALIZED_PAYMENT_ID = "{\"paymentId\":\"" + PAYMENT_ID + "\"}";

    private final Signer signer = mock(Signer.class);
    private final Map<String, BasicAuthenticationMean> authenticationMeans = BoursoramaGroupSampleMeans.getAuthMeans();

    @Autowired
    private RestTemplateManager restTemplateManager;

    @Autowired
    @Qualifier("BoursoramaPaymentProviderV3")
    private GenericPaymentProviderV2 boursoramaPaymentProviderV3;

    Stream<SepaPaymentProvider> getBoursoramaProviders() {
        return Stream.of(boursoramaPaymentProviderV3);
    }

    @BeforeEach
    public void setUp() {
        when(signer.sign(ArgumentMatchers.any(byte[].class), any(), ArgumentMatchers.any(SignatureAlgorithm.class)))
                .thenReturn(Base64.toBase64String("TEST-ENCODED-SIGNATURE".getBytes()));
    }

    @ParameterizedTest
    @MethodSource("getBoursoramaProviders")
    public void shouldThrowPaymentExecutionTechnicalExceptionForGetPaymentStatusWhen5xxError(SepaPaymentProvider sepaPaymentProvider) {
        // given
        GetStatusRequest request = new GetStatusRequestBuilder()
                .setRestTemplateManager(restTemplateManager)
                .setProviderState(SERIALIZED_PAYMENT_ID)
                .setSigner(signer)
                .setAuthenticationMeans(authenticationMeans)
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .build();

        // when
        final SepaPaymentStatusResponseDTO responseDTO = sepaPaymentProvider.getStatus(request);

        // then
        assertThat(responseDTO.getPaymentId()).isNotEmpty();
        assertThat(responseDTO.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("RJCT");
                    assertThat(statuses.getRawBankPaymentStatus().getReason()).isEmpty();
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.REJECTED);
                }));
    }

    @ParameterizedTest
    @MethodSource("getBoursoramaProviders")
    public void shouldReturnEmptyPaymentIdAndRejectedPaymentStatusAndProperStatusesInPecMetadataForSubmitPaymentWhen5xxError(SepaPaymentProvider sepaPaymentProvider) {
        // given
        SubmitPaymentRequest request = new SubmitPaymentRequestBuilder()
                .setRedirectUrlPostedBackFromSite(REDIRECT_URL_POSTED_BACK_FROM_SITE)
                .setRestTemplateManager(restTemplateManager)
                .setProviderState(SERIALIZED_PAYMENT_ID)
                .setSigner(signer)
                .setAuthenticationMeans(authenticationMeans)
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .build();

        // when
        SepaPaymentStatusResponseDTO responseDTO = sepaPaymentProvider.submitPayment(request);

        // then
        assertThat(responseDTO.getPaymentId()).isNotEmpty();
        assertThat(responseDTO.getPaymentExecutionContextMetadata()).satisfies(pecMetadata ->
                assertThat(pecMetadata.getPaymentStatuses()).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("RJCT");
                    assertThat(statuses.getRawBankPaymentStatus().getReason()).isEmpty();
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.REJECTED);
                }));
    }
}
