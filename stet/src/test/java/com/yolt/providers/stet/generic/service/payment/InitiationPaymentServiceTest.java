package com.yolt.providers.stet.generic.service.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.CreationFailedException;
import com.yolt.providers.common.pis.sepa.*;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.common.util.KeyUtil;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.config.DefaultProperties;
import com.yolt.providers.stet.generic.domain.Region;
import com.yolt.providers.stet.generic.domain.Scope;
import com.yolt.providers.stet.generic.dto.TestStetPaymentInitiationResponseDTO;
import com.yolt.providers.stet.generic.dto.TestStetPaymentStatusResponseDTO;
import com.yolt.providers.stet.generic.dto.TestTokenResponseDTO;
import com.yolt.providers.stet.generic.dto.payment.*;
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
class InitiationPaymentServiceTest {

    private static final String CERTIFICATE_PATH = "certificates/fake-certificate.pem";
    private static final String ACCESS_TOKEN = "fbdb711e-5d50-41d1-b4e2-eea0c3a80149";
    private static final String PSU_IP_ADDRESS = "127.0.0.1";
    private static final String TOKEN_URL = "https://test.com/token";

    private InitiationPaymentService paymentService;

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

    @BeforeEach
    void initialize() {
        ProviderStateMapper providerStateMapper = new DefaultProviderStateMapper(new ObjectMapper());
        paymentService = new InitiationPaymentService(restClient, paymentMapper, providerStateMapper, Scope.PISP, properties);
    }

    @Test
    void shouldReturnTokenResponseDTOAfterClientCredentialsTokenGrant() {
        // given
        List<Region> regions = createSingleRegion();
        TokenResponseDTO tokenResponseDTO = createTokenResponseDTO();
        DefaultAuthenticationMeans authMeans = createAuthenticationMeans();

        when(properties.getRegions())
                .thenReturn(regions);
        when(restClient.getClientToken(any(HttpClient.class), anyString(), any(DefaultAuthenticationMeans.class), any(), any()))
                .thenReturn(tokenResponseDTO);

        // when
        TokenResponseDTO tokenDTO = paymentService.getClientCredentialsToken(httpClient, authMeans, signer);

        // then
        assertThat(tokenDTO.getAccessToken()).isEqualTo(ACCESS_TOKEN);
    }

    @Test
    void shouldReturnAuthorizationUrlAndProviderStateWithPaymentIdAfterPaymentInitiation() {
        // given
        String authUrl = "https://test.com/payment?paymentRequestResourceId=1852";
        StetPaymentInitiationRequestDTO paymentInitiationRequestDTO = createStetPaymentInitiationRequestDTO();
        DefaultAuthenticationMeans authMeans = createAuthenticationMeans();
        InitiatePaymentRequest initiatePaymentRequest = createInitiatePaymentRequest();
        List<Region> regions = createSingleRegion();
        TokenResponseDTO tokenResponseDTO = createTokenResponseDTO();

        when(paymentMapper.mapToStetPaymentInitiationRequestDTO(any(InitiatePaymentRequest.class), any(DefaultAuthenticationMeans.class)))
                .thenReturn(paymentInitiationRequestDTO);
        when(restClient.initiatePayment(any(HttpClient.class), any(PaymentRequest.class), any(StetPaymentInitiationRequestDTO.class)))
                .thenReturn(createStetPaymentInitiationResponseDTO(authUrl));
        when(properties.getRegions())
                .thenReturn(regions);
        when(restClient.getClientToken(any(HttpClient.class), anyString(), any(DefaultAuthenticationMeans.class), any(), any()))
                .thenReturn(tokenResponseDTO);

        // when
        LoginUrlAndStateDTO loginUrlAndStateDTO = paymentService.initiatePayment(httpClient, initiatePaymentRequest, authMeans);

        // then
        assertThat(loginUrlAndStateDTO.getLoginUrl()).isEqualTo(authUrl);
        assertThat(loginUrlAndStateDTO.getProviderState()).isEqualTo(createJsonProviderState("1852"));
        verify(paymentMapper).mapToStetPaymentInitiationRequestDTO(initiatePaymentRequest, authMeans);
        verify(restClient).initiatePayment(eq(httpClient), paymentRequest.capture(), eq(paymentInitiationRequestDTO));
        assertThat(paymentRequest.getValue()).satisfies(validatePaymentRequest("/payment-requests", authMeans));
    }

    @Test
    void shouldThrowCreationFailedExceptionDueToMissingAuthorizationUrlAfterPaymentInitiation() {
        // given
        String missingAuthUrl = "";
        StetPaymentInitiationRequestDTO paymentInitiationRequestDTO = createStetPaymentInitiationRequestDTO();
        InitiatePaymentRequest initiatePaymentRequest = createInitiatePaymentRequest();
        DefaultAuthenticationMeans authMeans = createAuthenticationMeans();
        List<Region> regions = createSingleRegion();
        TokenResponseDTO tokenResponseDTO = createTokenResponseDTO();

        when(paymentMapper.mapToStetPaymentInitiationRequestDTO(any(InitiatePaymentRequest.class), any(DefaultAuthenticationMeans.class)))
                .thenReturn(paymentInitiationRequestDTO);
        when(restClient.initiatePayment(any(HttpClient.class), any(PaymentRequest.class), any(StetPaymentInitiationRequestDTO.class)))
                .thenReturn(createStetPaymentInitiationResponseDTO(missingAuthUrl));
        when(properties.getRegions())
                .thenReturn(regions);
        when(restClient.getClientToken(any(HttpClient.class), anyString(), any(DefaultAuthenticationMeans.class), any(), any()))
                .thenReturn(tokenResponseDTO);

        // when
        ThrowingCallable throwingCallable = () -> paymentService.initiatePayment(httpClient, initiatePaymentRequest, authMeans);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(CreationFailedException.class);
        verify(paymentMapper).mapToStetPaymentInitiationRequestDTO(initiatePaymentRequest, authMeans);
        verify(restClient).initiatePayment(eq(httpClient), paymentRequest.capture(), eq(paymentInitiationRequestDTO));
        assertThat(paymentRequest.getValue()).satisfies(validatePaymentRequest("/payment-requests", authMeans));
    }

    @Test
    void shouldThrowCreationFailedExceptionDueToMissingPaymentRequestResourceIdAfterPaymentInitiation() {
        // given
        String authUrlWithMissingPaymentRequestResourceId = "https://test.com/payment";
        StetPaymentInitiationRequestDTO paymentInitiationRequestDTO = createStetPaymentInitiationRequestDTO();
        InitiatePaymentRequest initiatePaymentRequest = createInitiatePaymentRequest();
        DefaultAuthenticationMeans authMeans = createAuthenticationMeans();
        List<Region> regions = createSingleRegion();
        TokenResponseDTO tokenResponseDTO = createTokenResponseDTO();

        when(paymentMapper.mapToStetPaymentInitiationRequestDTO(any(InitiatePaymentRequest.class), any(DefaultAuthenticationMeans.class)))
                .thenReturn(paymentInitiationRequestDTO);
        when(restClient.initiatePayment(any(HttpClient.class), any(PaymentRequest.class), any(StetPaymentInitiationRequestDTO.class)))
                .thenReturn(createStetPaymentInitiationResponseDTO(authUrlWithMissingPaymentRequestResourceId));
        when(properties.getRegions())
                .thenReturn(regions);
        when(restClient.getClientToken(any(HttpClient.class), anyString(), any(DefaultAuthenticationMeans.class), any(), any()))
                .thenReturn(tokenResponseDTO);

        // when
        ThrowingCallable throwingCallable = () -> paymentService.initiatePayment(httpClient, initiatePaymentRequest, authMeans);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(CreationFailedException.class);
        verify(paymentMapper).mapToStetPaymentInitiationRequestDTO(initiatePaymentRequest, authMeans);
        verify(restClient).initiatePayment(eq(httpClient), paymentRequest.capture(), eq(paymentInitiationRequestDTO));
        assertThat(paymentRequest.getValue()).satisfies(validatePaymentRequest("/payment-requests", authMeans));
    }

    @Test
    void shouldReturnPaymentRequestResourceIdFromAuthorizationUrl() {
        // given
        String authUrl = "https://test.com/payment?paymentRequestResourceId=3811";

        // when
        String paymentRequestResourceId = paymentService.getPaymentRequestResourceId(authUrl);

        // then
        assertThat(paymentRequestResourceId).isEqualTo("3811");
    }

    @Test
    void shouldReturnSepaPaymentStatusAndPaymentIdAfterPaymentConfirmation() {
        // given
        String callbackUrl = "https://yolt.com/payment?psuAuthenticationFactor=1166";
        StetPaymentStatusResponseDTO paymentStatusResponseDTO = createStetPaymentStatusResponseDTO();
        SubmitPaymentRequest submitPaymentRequest = createSubmitPaymentRequest(callbackUrl, "9911");
        DefaultAuthenticationMeans authMeans = createAuthenticationMeans();
        List<Region> regions = createSingleRegion();
        TokenResponseDTO tokenResponseDTO = createTokenResponseDTO();

        when(restClient.getPaymentStatus(any(HttpClient.class), any(PaymentRequest.class)))
                .thenReturn(paymentStatusResponseDTO);
        when(paymentMapper.mapToSepaPaymentStatus(StetPaymentStatus.ACCP))
                .thenReturn(SepaPaymentStatus.ACCEPTED);
        when(properties.getRegions())
                .thenReturn(regions);
        when(restClient.getClientToken(any(HttpClient.class), anyString(), any(DefaultAuthenticationMeans.class), any(), any()))
                .thenReturn(tokenResponseDTO);

        // when
        SepaPaymentStatusResponseDTO responseDTO = paymentService.confirmPayment(httpClient, submitPaymentRequest, authMeans);

        // then
        assertThat(responseDTO.getPaymentId()).isEqualTo("9911");

        verify(restClient).getPaymentStatus(eq(httpClient), paymentRequest.capture());
        assertThat(paymentRequest.getValue()).satisfies(validatePaymentRequest("/payment-requests/9911", authMeans));
        verify(paymentMapper).mapToSepaPaymentStatus(StetPaymentStatus.ACCP);
    }

    @Test
    void shouldReturnSepaPaymentStatusAndPaymentIdAfterPaymentStatusVerification() {
        // given
        StetPaymentStatusResponseDTO paymentStatusResponseDTO = createStetPaymentStatusResponseDTO();
        GetStatusRequest getStatusRequest = createGetStatusRequest("7676");
        DefaultAuthenticationMeans authMeans = createAuthenticationMeans();
        List<Region> regions = createSingleRegion();
        TokenResponseDTO tokenResponseDTO = createTokenResponseDTO();

        when(restClient.getPaymentStatus(any(HttpClient.class), any(PaymentRequest.class)))
                .thenReturn(paymentStatusResponseDTO);
        when(paymentMapper.mapToSepaPaymentStatus(StetPaymentStatus.ACCP))
                .thenReturn(SepaPaymentStatus.ACCEPTED);
        when(properties.getRegions())
                .thenReturn(regions);
        when(restClient.getClientToken(any(HttpClient.class), anyString(), any(DefaultAuthenticationMeans.class), any(), any()))
                .thenReturn(tokenResponseDTO);

        // when
        SepaPaymentStatusResponseDTO responseDTO = paymentService.getPaymentStatus(httpClient, getStatusRequest, authMeans);

        // then
        assertThat(responseDTO.getPaymentId()).isEqualTo("7676");
        verify(restClient).getPaymentStatus(eq(httpClient), paymentRequest.capture());
        assertThat(paymentRequest.getValue()).satisfies(validatePaymentRequest("/payment-requests/7676", authMeans));
        verify(paymentMapper).mapToSepaPaymentStatus(StetPaymentStatus.ACCP);
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

    private StetPaymentInitiationRequestDTO createStetPaymentInitiationRequestDTO() {
        return StetPaymentInitiationRequestDTO.builder()
                .chargeBearer(StetChargeBearer.SLEV)
                .build();
    }

    private StetPaymentInitiationResponseDTO createStetPaymentInitiationResponseDTO(String authorizationUrl) {
        return TestStetPaymentInitiationResponseDTO.builder()
                .consentApprovalHref(authorizationUrl)
                .build();
    }

    private StetPaymentStatusResponseDTO createStetPaymentStatusResponseDTO() {
        return TestStetPaymentStatusResponseDTO.builder()
                .paymentStatus(StetPaymentStatus.ACCP)
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

    private InitiatePaymentRequest createInitiatePaymentRequest() {
        return new InitiatePaymentRequestBuilder()
                .setBaseClientRedirectUrl("https://yolt.com/payment")
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .setSigner(signer)
                .setState("76cea255-fa20-4108-93a6-34d5032b39ed")
                .setRequestDTO(SepaInitiatePaymentRequestDTO.builder().build())
                .build();
    }

    private SubmitPaymentRequest createSubmitPaymentRequest(String callbackUrl, String paymentId) {
        return new SubmitPaymentRequestBuilder()
                .setRedirectUrlPostedBackFromSite(callbackUrl)
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .setSigner(signer)
                .setProviderState(createJsonProviderState(paymentId))
                .build();
    }

    private GetStatusRequest createGetStatusRequest(String paymentId) {
        return new GetStatusRequestBuilder()
                .setPaymentId(paymentId)
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .setSigner(signer)
                .build();
    }

    private String createJsonProviderState(String paymentId) {
        return "{\"paymentId\":\"" + paymentId + "\"}";
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
