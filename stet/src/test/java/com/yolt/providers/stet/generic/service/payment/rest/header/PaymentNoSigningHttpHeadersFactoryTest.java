package com.yolt.providers.stet.generic.service.payment.rest.header;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.pis.sepa.SepaInitiatePaymentRequestDTO;
import com.yolt.providers.common.util.KeyUtil;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.domain.SignatureData;
import com.yolt.providers.stet.generic.service.payment.request.PaymentRequest;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

import static com.yolt.providers.stet.generic.domain.HttpHeadersExtension.PSU_IP_ADDRESS;
import static com.yolt.providers.stet.generic.domain.HttpHeadersExtension.X_REQUEST_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.cloud.contract.spec.internal.MediaTypes.APPLICATION_FORM_URLENCODED;
import static org.springframework.cloud.contract.spec.internal.MediaTypes.APPLICATION_JSON;
import static org.springframework.http.HttpHeaders.*;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

/**
 * @deprecated It should be removed after migration of all STET generic providers to PEC (Payment Execution Context).
 * TODO: Required tickets to be done before deleting this class: C4PO-8635, C4PO-8457, C4PO-8451
 */
@Deprecated
@ExtendWith(MockitoExtension.class)
class PaymentNoSigningHttpHeadersFactoryTest {

    private static final String CERTIFICATE_PATH = "certificates/fake-certificate.pem";
    private static final String ACCESS_TOKEN = "b6b4ffe8-5e60-4ef9-858f-78b48c5d7b11";
    private static final String LAST_EXTERNAL_TRACE_ID = "8bf52479-f18c-40cd-a1f3-3e667015f05a";
    private static final String PSU_IP_ADDRESS_VALUE = "127.0.0.1";

    @Mock
    private Signer signer;

    private PaymentNoSigningHttpHeadersFactory httpHeadersFactory;

    @BeforeEach
    void initialize() {
        Supplier<String> lastExternalTraceIdSupplier = () -> LAST_EXTERNAL_TRACE_ID;
        httpHeadersFactory = new PaymentNoSigningHttpHeadersFactory(lastExternalTraceIdSupplier);
    }

    @Test
    void shouldReturnSpecifiedNoSigningHeadersForGettingClientToken() {
        // given
        DefaultAuthenticationMeans authMeans = createDefaultAuthenticationMeans();

        // when
        HttpHeaders headers = httpHeadersFactory.createClientTokenHeaders(authMeans, null, signer, "/token");

        // then
        Map<String, String> headersMap = headers.toSingleValueMap();
        assertThat(headersMap).hasSize(3);
        assertThat(headersMap).containsKey(ACCEPT).containsValue(APPLICATION_JSON);
        assertThat(headersMap).containsKey(AUTHORIZATION).containsValue("Basic OTY0MTM3MWMtNWEwNi00MmE4LTg2YjgtNWViMWVlMGZmM2FiOmY5MTdjOGY0LTBkMmYtNDZiZS1iODU0LTg4YzJiYWQxMDNjOA==");
        assertThat(headersMap).containsKey(CONTENT_TYPE).containsValue(APPLICATION_FORM_URLENCODED);
    }

    @Test
    void shouldReturnSpecifiedNoSigningHeadersForPaymentInitiation() {
        // given
        String url = "/payment-requests";
        DefaultAuthenticationMeans authMeans = createDefaultAuthenticationMeans();
        PaymentRequest paymentRequest = createPaymentRequest(url, authMeans);
        SepaInitiatePaymentRequestDTO initiatePaymentRequestDTO = createSepaInitiatePaymentRequestDTO();
        SignatureData signatureData = createSignatureData(url, POST, authMeans);

        // when
        HttpHeaders headers = httpHeadersFactory.createPaymentInitiationHeaders(signatureData.getHttpMethod(), paymentRequest, initiatePaymentRequestDTO);

        // then
        Map<String, String> headersMap = headers.toSingleValueMap();
        assertThat(headersMap).hasSize(5);
        assertThat(headersMap).containsKey(ACCEPT).containsValue(APPLICATION_JSON);
        assertThat(headersMap).containsKey(AUTHORIZATION).containsValue("Bearer " + ACCESS_TOKEN);
        assertThat(headersMap).containsKey(CONTENT_TYPE).containsValue(APPLICATION_JSON);
        assertThat(headersMap).containsKey(PSU_IP_ADDRESS).containsValue(PSU_IP_ADDRESS_VALUE);
        assertThat(headersMap).containsKey(X_REQUEST_ID).containsValue(LAST_EXTERNAL_TRACE_ID);
    }

    @Test
    void shouldReturnSpecifiedNoSigningHeadersForPaymentConfirmation() {
        // given
        String url = "/payment-requests/1/confirmation";
        DefaultAuthenticationMeans authMeans = createDefaultAuthenticationMeans();
        PaymentRequest paymentRequest = createPaymentRequest(url, authMeans);
        SepaInitiatePaymentRequestDTO initiatePaymentRequestDTO = createSepaInitiatePaymentRequestDTO();
        SignatureData signatureData = createSignatureData(url, POST, authMeans);

        // when
        HttpHeaders headers = httpHeadersFactory.createPaymentConfirmationHeaders(signatureData.getHttpMethod(), paymentRequest, initiatePaymentRequestDTO);

        // then
        Map<String, String> headersMap = headers.toSingleValueMap();
        assertThat(headersMap).hasSize(5);
        assertThat(headersMap).containsKey(ACCEPT).containsValue(APPLICATION_JSON);
        assertThat(headersMap).containsKey(AUTHORIZATION).containsValue("Bearer " + ACCESS_TOKEN);
        assertThat(headersMap).containsKey(CONTENT_TYPE).containsValue(APPLICATION_JSON);
        assertThat(headersMap).containsKey(PSU_IP_ADDRESS).containsValue(PSU_IP_ADDRESS_VALUE);
        assertThat(headersMap).containsKey(X_REQUEST_ID).containsValue(LAST_EXTERNAL_TRACE_ID);
    }

    @Test
    void shouldReturnSpecifiedNoSigningHeadersForGettingPaymentStatus() {
        // given
        String url = "/payment-requests/1";
        DefaultAuthenticationMeans authMeans = createDefaultAuthenticationMeans();
        PaymentRequest paymentRequest = createPaymentRequest(url, authMeans);
        SignatureData signatureData = createSignatureData(url, GET, authMeans);

        // when
        HttpHeaders headers = httpHeadersFactory.createPaymentStatusHeaders(signatureData.getHttpMethod(), paymentRequest);

        // then
        Map<String, String> headersMap = headers.toSingleValueMap();
        assertThat(headersMap).hasSize(5);
        assertThat(headersMap).containsKey(ACCEPT).containsValue(APPLICATION_JSON);
        assertThat(headersMap).containsKey(AUTHORIZATION).containsValue("Bearer " + ACCESS_TOKEN);
        assertThat(headersMap).containsKey(CONTENT_TYPE).containsValue(APPLICATION_JSON);
        assertThat(headersMap).containsKey(PSU_IP_ADDRESS).containsValue(PSU_IP_ADDRESS_VALUE);
        assertThat(headersMap).containsKey(X_REQUEST_ID).containsValue(LAST_EXTERNAL_TRACE_ID);
    }

    private DefaultAuthenticationMeans createDefaultAuthenticationMeans() {
        return DefaultAuthenticationMeans.builder()
                .clientId("9641371c-5a06-42a8-86b8-5eb1ee0ff3ab")
                .clientSecret("f917c8f4-0d2f-46be-b854-88c2bad103c8")
                .clientSigningCertificate(readCertificate())
                .clientSigningKeyId(UUID.randomUUID())
                .build();
    }

    private SepaInitiatePaymentRequestDTO createSepaInitiatePaymentRequestDTO() {
        return SepaInitiatePaymentRequestDTO.builder()
                .creditorName("CreditorName")
                .build();
    }

    private PaymentRequest createPaymentRequest(String url, DefaultAuthenticationMeans authMeans) {
        return new PaymentRequest(url, ACCESS_TOKEN, signer, PSU_IP_ADDRESS_VALUE, authMeans);
    }

    @SneakyThrows
    private SignatureData createSignatureData(String url, HttpMethod method, DefaultAuthenticationMeans authMeans) {
        return new SignatureData(signer, authMeans.getSigningKeyIdHeader(), authMeans.getClientSigningKeyId(), authMeans.getClientSigningCertificate(), method, url);
    }

    @SneakyThrows
    private X509Certificate readCertificate() {
        URL certificateUrl = this.getClass().getClassLoader().getResource(CERTIFICATE_PATH);
        String certificatePem = new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(certificateUrl).toURI())));
        return KeyUtil.createCertificateFromPemFormat(certificatePem);
    }
}
