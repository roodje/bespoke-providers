package com.yolt.providers.stet.generic.service.payment.rest;

import com.yolt.providers.common.constants.OAuth;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.domain.ExecutionInfo;
import com.yolt.providers.stet.generic.domain.Scope;
import com.yolt.providers.stet.generic.dto.TestStetPaymentConfirmationResponseDTO;
import com.yolt.providers.stet.generic.dto.TestStetPaymentInitiationResponseDTO;
import com.yolt.providers.stet.generic.dto.TestStetPaymentStatusResponseDTO;
import com.yolt.providers.stet.generic.dto.TestTokenResponseDTO;
import com.yolt.providers.stet.generic.dto.payment.*;
import com.yolt.providers.stet.generic.dto.token.TokenResponseDTO;
import com.yolt.providers.stet.generic.http.error.ExecutionSupplier;
import com.yolt.providers.stet.generic.service.payment.request.PaymentRequest;
import com.yolt.providers.stet.generic.service.payment.rest.error.DefaultPaymentHttpErrorHandler;
import com.yolt.providers.stet.generic.service.payment.rest.header.PaymentHttpHeadersFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static com.yolt.providers.common.rest.http.ProviderClientEndpoints.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

/**
 * @deprecated It should be removed after migration of all STET generic providers to PEC (Payment Execution Context).
 * TODO: Required tickets to be done before deleting this class: C4PO-8635, C4PO-8457, C4PO-8451
 */
@Deprecated
@ExtendWith(MockitoExtension.class)
class DefaultPaymentRestClientTest {

    private static final String ACCESS_TOKEN = "942ffacc-a81a-4647-ada1-b9d0455973dc";
    private static final String PSU_IP_ADDRESS = "127.0.0.1";

    @Mock
    private PaymentHttpHeadersFactory headersFactory;

    @Mock
    private DefaultPaymentHttpErrorHandler errorHandler;

    @Mock
    private HttpClient httpClient;

    @Mock
    private Signer signer;

    @Captor
    private ArgumentCaptor<ExecutionSupplier<?>> executionSupplierArgumentCaptor;

    @Captor
    private ArgumentCaptor<ExecutionInfo> executionInfoArgumentCaptor;

    @InjectMocks
    private DefaultPaymentRestClient restClient;

    @Test
    void shouldReturnCorrectResponseForPaymentInitiation() {
        // given
        String endpoint = "/payment-requests";
        PaymentRequest paymentRequest = createPaymentRequest(endpoint);
        StetPaymentInitiationRequestDTO paymentInitiationRequestDTO = createStetPaymentInitiationRequestDTO();
        HttpHeaders expectedHeaders = createHttpHeaders();
        StetPaymentInitiationResponseDTO expectedPaymentInitiationResponseDTO = createStetPaymentInitiationResponseDTO();
        ExecutionInfo expectedExecutionInfo = new ExecutionInfo(endpoint, POST, expectedHeaders, INITIATE_PAYMENT);

        when(headersFactory.createPaymentInitiationHeaders(any(HttpMethod.class), any(PaymentRequest.class), any(StetPaymentInitiationRequestDTO.class)))
                .thenReturn(expectedHeaders);
        when(errorHandler.executeAndHandle(ArgumentMatchers.<ExecutionSupplier<StetPaymentInitiationResponseDTO>>any(), any(ExecutionInfo.class)))
                .thenReturn(expectedPaymentInitiationResponseDTO);

        // when
        StetPaymentInitiationResponseDTO stetPaymentInitiationResponseDTO = restClient
                .initiatePayment(httpClient, paymentRequest, paymentInitiationRequestDTO);

        // then
        assertThat(stetPaymentInitiationResponseDTO).isEqualTo(expectedPaymentInitiationResponseDTO);
        verify(headersFactory).createPaymentInitiationHeaders(POST, paymentRequest, paymentInitiationRequestDTO);
        verify(errorHandler).executeAndHandle(executionSupplierArgumentCaptor.capture(), executionInfoArgumentCaptor.capture());
        assertThat(executionSupplierArgumentCaptor.getValue()).isNotNull();
        assertThat(executionInfoArgumentCaptor.getValue()).isEqualToComparingFieldByField(expectedExecutionInfo);
    }

    @Test
    void shouldReturnCorrectResponseForPaymentConfirmation() {
        // given
        String endpoint = "/payment-requests/2811/confirmation";
        PaymentRequest paymentRequest = createPaymentRequest(endpoint);
        StetPaymentConfirmationRequestDTO paymentConfirmationRequestDTO = createStetPaymentConfirmationRequestDTO();
        HttpHeaders expectedHeaders = createHttpHeaders();
        StetPaymentConfirmationResponseDTO expectedPaymentConfirmationResponseDTO = createStetPaymentConfirmationResponseDTO();
        ExecutionInfo expectedExecutionInfo = new ExecutionInfo(endpoint, POST, expectedHeaders, SUBMIT_PAYMENT);

        when(headersFactory.createPaymentConfirmationHeaders(any(HttpMethod.class), any(PaymentRequest.class), any(StetPaymentConfirmationRequestDTO.class)))
                .thenReturn(expectedHeaders);
        when(errorHandler.executeAndHandle(ArgumentMatchers.<ExecutionSupplier<StetPaymentConfirmationResponseDTO>>any(), any(ExecutionInfo.class)))
                .thenReturn(expectedPaymentConfirmationResponseDTO);

        // when
        StetPaymentConfirmationResponseDTO stetPaymentConfirmationResponseDTO = restClient
                .confirmPayment(httpClient, paymentRequest, paymentConfirmationRequestDTO);

        // then
        assertThat(expectedPaymentConfirmationResponseDTO).isEqualTo(stetPaymentConfirmationResponseDTO);
        verify(headersFactory).createPaymentConfirmationHeaders(POST, paymentRequest, paymentConfirmationRequestDTO);
        verify(errorHandler).executeAndHandle(executionSupplierArgumentCaptor.capture(), executionInfoArgumentCaptor.capture());
        assertThat(executionSupplierArgumentCaptor.getValue()).isNotNull();
        assertThat(executionInfoArgumentCaptor.getValue()).isEqualToComparingFieldByField(expectedExecutionInfo);
    }

    @Test
    void shouldReturnCorrectResponseForGettingPaymentStatus() {
        // given
        String endpoint = "/payment-requests/6644";
        PaymentRequest paymentRequest = createPaymentRequest(endpoint);
        HttpHeaders expectedHeaders = createHttpHeaders();
        StetPaymentStatusResponseDTO expectedPaymentStatusResponseDTO = createStetPaymentStatusResponseDTO();
        ExecutionInfo expectedExecutionInfo = new ExecutionInfo(endpoint, GET, expectedHeaders, GET_PAYMENT_STATUS);

        when(headersFactory.createPaymentStatusHeaders(any(HttpMethod.class), any(PaymentRequest.class)))
                .thenReturn(expectedHeaders);
        when(errorHandler.executeAndHandle(ArgumentMatchers.<ExecutionSupplier<StetPaymentStatusResponseDTO>>any(), any(ExecutionInfo.class)))
                .thenReturn(expectedPaymentStatusResponseDTO);

        // when
        StetPaymentStatusResponseDTO stetPaymentStatusResponseDTO = restClient.getPaymentStatus(httpClient, paymentRequest);

        // then
        assertThat(stetPaymentStatusResponseDTO).isEqualTo(expectedPaymentStatusResponseDTO);
        verify(headersFactory).createPaymentStatusHeaders(GET, paymentRequest);
        verify(errorHandler).executeAndHandle(executionSupplierArgumentCaptor.capture(), executionInfoArgumentCaptor.capture());
        assertThat(executionSupplierArgumentCaptor.getValue()).isNotNull();
        assertThat(executionInfoArgumentCaptor.getValue()).isEqualToComparingFieldByField(expectedExecutionInfo);
    }

    @Test
    void shouldReturnCorrectResponseForClientTokenGrant() {
        // given
        String endpoint = "/token";
        DefaultAuthenticationMeans authMeans = createDefaultAuthenticationMeans();
        MultiValueMap<String, String> body = createClientTokenRequestBody();
        HttpHeaders expectedHeaders = createHttpHeaders();
        TokenResponseDTO expectedTokenResponseDTO = createTokenResponseDTO();
        ExecutionInfo expectedExecutionInfo = new ExecutionInfo(endpoint, POST, expectedHeaders, CLIENT_CREDENTIALS_GRANT);

        when(headersFactory.createClientTokenHeaders(any(DefaultAuthenticationMeans.class), any(), any(), any()))
                .thenReturn(expectedHeaders);
        when(errorHandler.executeAndHandle(ArgumentMatchers.<ExecutionSupplier<TokenResponseDTO>>any(), any(ExecutionInfo.class)))
                .thenReturn(expectedTokenResponseDTO);

        // when
        TokenResponseDTO clientTokenDTO = restClient.getClientToken(httpClient, endpoint, authMeans, body, signer);

        // then
        assertThat(clientTokenDTO).isEqualTo(expectedTokenResponseDTO);
        verify(headersFactory).createClientTokenHeaders(authMeans, body, signer, endpoint);
        verify(errorHandler).executeAndHandle(executionSupplierArgumentCaptor.capture(), executionInfoArgumentCaptor.capture());
        assertThat(executionSupplierArgumentCaptor.getValue()).isNotNull();
        assertThat(executionInfoArgumentCaptor.getValue()).isEqualToComparingFieldByField(expectedExecutionInfo);
    }

    private PaymentRequest createPaymentRequest(String url) {
        DefaultAuthenticationMeans authMeans = createDefaultAuthenticationMeans();
        return new PaymentRequest(url, ACCESS_TOKEN, signer, PSU_IP_ADDRESS, authMeans);
    }

    private HttpHeaders createHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(ACCESS_TOKEN);
        return headers;
    }

    private StetPaymentInitiationRequestDTO createStetPaymentInitiationRequestDTO() {
        return StetPaymentInitiationRequestDTO.builder()
                .chargeBearer(StetChargeBearer.SLEV)
                .build();
    }

    private StetPaymentConfirmationRequestDTO createStetPaymentConfirmationRequestDTO() {
        return StetPaymentConfirmationRequestDTO.builder()
                .psuAuthenticationFactor("8572")
                .build();
    }

    private MultiValueMap<String, String> createClientTokenRequestBody() {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add(OAuth.GRANT_TYPE, OAuth.CLIENT_CREDENTIALS);
        body.add(OAuth.SCOPE, Scope.PISP.getValue());
        return body;
    }

    private StetPaymentInitiationResponseDTO createStetPaymentInitiationResponseDTO() {
        return TestStetPaymentInitiationResponseDTO.builder()
                .consentApprovalHref("https://test.com/payment/approval")
                .build();
    }

    private StetPaymentConfirmationResponseDTO createStetPaymentConfirmationResponseDTO() {
        return TestStetPaymentConfirmationResponseDTO.builder()
                .paymentStatus(StetPaymentStatus.ACCP)
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

    private DefaultAuthenticationMeans createDefaultAuthenticationMeans() {
        return DefaultAuthenticationMeans.builder()
                .clientId("9641371c-5a06-42a8-86b8-5eb1ee0ff3ab")
                .clientSecret("f917c8f4-0d2f-46be-b854-88c2bad103c8")
                .build();
    }
}
