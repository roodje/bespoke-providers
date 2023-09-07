package com.yolt.providers.stet.generic.service.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.ConfirmationFailedException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.pis.sepa.SepaPaymentStatus;
import com.yolt.providers.common.pis.sepa.SepaPaymentStatusResponseDTO;
import com.yolt.providers.common.pis.sepa.SubmitPaymentRequest;
import com.yolt.providers.common.pis.sepa.SubmitPaymentRequestBuilder;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.common.util.KeyUtil;
import com.yolt.providers.stet.generic.dto.TestStetPaymentConfirmationResponseDTO;
import com.yolt.providers.stet.generic.dto.TestTokenResponseDTO;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.config.DefaultProperties;
import com.yolt.providers.stet.generic.domain.Region;
import com.yolt.providers.stet.generic.domain.Scope;
import com.yolt.providers.stet.generic.dto.payment.StetPaymentConfirmationRequestDTO;
import com.yolt.providers.stet.generic.dto.payment.StetPaymentConfirmationResponseDTO;
import com.yolt.providers.stet.generic.dto.payment.StetPaymentStatus;
import com.yolt.providers.stet.generic.dto.token.TokenResponseDTO;
import com.yolt.providers.stet.generic.mapper.payment.PaymentMapper;
import com.yolt.providers.stet.generic.mapper.providerstate.DefaultProviderStateMapper;
import com.yolt.providers.stet.generic.mapper.providerstate.ProviderStateMapper;
import com.yolt.providers.stet.generic.service.payment.request.PaymentRequest;
import com.yolt.providers.stet.generic.service.payment.rest.PaymentRestClient;
import lombok.SneakyThrows;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @deprecated It should be removed after migration of all STET generic providers to PEC (Payment Execution Context).
 * TODO: Required tickets to be done before deleting this class: C4PO-8635, C4PO-8457, C4PO-8451
 */
@Deprecated
@ExtendWith(MockitoExtension.class)
class InitiationAndConfirmationPaymentServiceTest {

    private static final String CERTIFICATE_PATH = "certificates/fake-certificate.pem";
    private static final String ACCESS_TOKEN = "fbdb711e-5d50-41d1-b4e2-eea0c3a80149";
    private static final String PSU_IP_ADDRESS = "127.0.0.1";
    private static final String TOKEN_URL = "https://test.com/token";

    private InitiationAndConfirmationPaymentService paymentService;

    @Mock
    private PaymentRestClient restClient;

    @Mock
    private Signer signer;

    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private DefaultProperties properties;

    @Mock
    private HttpClient httpClient;

    @Captor
    private ArgumentCaptor<PaymentRequest> paymentRequest;

    @Captor
    private ArgumentCaptor<StetPaymentConfirmationRequestDTO> paymentConfirmationRequestDTO;

    @BeforeEach
    void initialize() {
        ProviderStateMapper providerStateMapper = new DefaultProviderStateMapper(new ObjectMapper());
        paymentService = new InitiationAndConfirmationPaymentService(restClient, paymentMapper, providerStateMapper, Scope.PISP, properties);
    }

    @Test
    void shouldReturnSepaPaymentStatusAndPaymentIdAfterPaymentConfirmation() {
        // given
        String callbackUrl = "https://yolt.com/payment?psuAuthenticationFactor=8752";
        SubmitPaymentRequest submitPaymentRequest = createSubmitPaymentRequest(callbackUrl, "5721");
        DefaultAuthenticationMeans authMeans = createAuthenticationMeans();
        List<Region> regions = createSingleRegion();
        TokenResponseDTO tokenResponseDTO = createTokenResponseDTO();

        when(restClient.confirmPayment(any(HttpClient.class), any(PaymentRequest.class), any(StetPaymentConfirmationRequestDTO.class)))
                .thenReturn(createStetPaymentConfirmationResponseDTO());
        when(paymentMapper.mapToSepaPaymentStatus(StetPaymentStatus.ACCP))
                .thenReturn(SepaPaymentStatus.ACCEPTED);
        when(properties.getRegions())
                .thenReturn(regions);
        when(restClient.getClientToken(any(HttpClient.class), anyString(), any(DefaultAuthenticationMeans.class), any(), any()))
                .thenReturn(tokenResponseDTO);

        // when
        SepaPaymentStatusResponseDTO responseDTO = paymentService.confirmPayment(httpClient, submitPaymentRequest, authMeans);

        // then
        assertThat(responseDTO.getPaymentId()).isEqualTo("5721");
        verify(restClient).confirmPayment(eq(httpClient), paymentRequest.capture(), paymentConfirmationRequestDTO.capture());
        assertThat(paymentRequest.getValue()).satisfies(validatePaymentRequest("/payment-requests/5721/confirmation", authMeans));
        assertThat(paymentConfirmationRequestDTO.getValue().getPsuAuthenticationFactor()).isEqualTo("8752");
        verify(paymentMapper).mapToSepaPaymentStatus(StetPaymentStatus.ACCP);
    }

    @Test
    void shouldThrowConfirmationFailedExceptionDueToMissingPsuAuthenticationFactorAfterPaymentConfirmation() {
        // given
        String callbackUrl = "https://yolt.com/payment";
        SubmitPaymentRequest submitPaymentRequest = createSubmitPaymentRequest(callbackUrl, "1144");
        DefaultAuthenticationMeans authMeans = createAuthenticationMeans();

        // when
        ThrowingCallable throwingCallable = () -> paymentService.confirmPayment(httpClient, submitPaymentRequest, authMeans);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(ConfirmationFailedException.class);
    }

    @Test
    void shouldThrowConfirmationFailedExceptionDueToErrorInCallbackUrlAfterPaymentConfirmation() {
        // given
        String callbackUrl = "https://yolt.com/payment?psuAuthenticationFactor=9992&error=warn";
        SubmitPaymentRequest submitPaymentRequest = createSubmitPaymentRequest(callbackUrl, "6882");
        DefaultAuthenticationMeans authMeans = createAuthenticationMeans();

        // when
        ThrowingCallable throwingCallable = () -> paymentService.confirmPayment(httpClient, submitPaymentRequest, authMeans);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(ConfirmationFailedException.class);
    }

    private Consumer<PaymentRequest> validatePaymentRequest(String url, DefaultAuthenticationMeans authMeans) {
        return (paymentRequest) -> {
            assertThat(paymentRequest.getUrl()).isEqualTo(url);
            assertThat(paymentRequest.getAccessToken()).isEqualTo(ACCESS_TOKEN);
            assertThat(paymentRequest.getPsuIpAddress()).isEqualTo(PSU_IP_ADDRESS);
            assertThat(paymentRequest.getSigner()).isEqualTo(signer);
            assertThat(paymentRequest.getAuthMeans()).isEqualTo(authMeans);
        };
    }

    private StetPaymentConfirmationResponseDTO createStetPaymentConfirmationResponseDTO() {
        return TestStetPaymentConfirmationResponseDTO.builder()
                .paymentStatus(StetPaymentStatus.ACCP)
                .build();
    }

    private SubmitPaymentRequest createSubmitPaymentRequest(String callbackUrl, String paymentId) {
        return new SubmitPaymentRequestBuilder()
                .setRedirectUrlPostedBackFromSite(callbackUrl)
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .setSigner(signer)
                .setProviderState("{\"paymentId\":\"" + paymentId + "\"}")
                .build();
    }

    private TokenResponseDTO createTokenResponseDTO() {
        return TestTokenResponseDTO.builder()
                .accessToken(ACCESS_TOKEN)
                .build();
    }

    private List<Region> createSingleRegion() {
        Region region = new Region();
        region.setTokenUrl(TOKEN_URL);
        return Collections.singletonList(region);
    }

    @SneakyThrows
    private DefaultAuthenticationMeans createAuthenticationMeans() {
        return DefaultAuthenticationMeans.builder()
                .signingKeyIdHeader("HeaderKeyId")
                .clientSigningKeyId(UUID.randomUUID())
                .clientSigningCertificate(readCertificate())
                .build();
    }

    @SneakyThrows
    private X509Certificate readCertificate() {
        URL certificateUrl = this.getClass().getClassLoader().getResource(CERTIFICATE_PATH);
        String certificatePem = new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(certificateUrl).toURI())));
        return KeyUtil.createCertificateFromPemFormat(certificatePem);
    }
}
