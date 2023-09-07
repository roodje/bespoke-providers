package com.yolt.providers.stet.generic.service.pec.common;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.domain.HttpHeadersExtension;
import com.yolt.providers.stet.generic.domain.SignatureData;
import com.yolt.providers.stet.generic.dto.payment.StetPaymentConfirmationRequestDTO;
import com.yolt.providers.stet.generic.dto.payment.StetPaymentInitiationRequestDTO;
import com.yolt.providers.stet.generic.http.signer.HttpSigner;
import com.yolt.providers.stet.generic.service.pec.authorization.token.StetTokenPaymentPreExecutionResult;
import com.yolt.providers.stet.generic.service.pec.confirmation.StetConfirmationPreExecutionResult;
import com.yolt.providers.stet.generic.service.pec.initiate.StetInitiatePreExecutionResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class StetSigningPaymentHttpHeadersFactoryTest {

    private static final String CLIENT_ID = "client-id";
    private static final String CLIENT_SECRET = "client-secret";
    private static final String BASIC_AUTHORIZATION = "Basic Y2xpZW50LWlkOmNsaWVudC1zZWNyZXQ=";
    private static final String ACCESS_TOKEN = "c0cc8fdf-1753-4604-a2c7-a4d11d642a0c";
    private static final String PSU_IP_ADDRESS = "127.0.0.1";
    private static final String LAST_EXTERNAL_TRACE_ID = "5e5421a6-1de8-4974-965d-410750e7d180";
    private static final String PAYMENT_PATH = "/payment-requests";
    private static final String SIGNING_KEY_HEADER_ID = "56833800-0e6a-4264-bd3d-050f1cca7c08";
    private static final UUID CLIENT_SIGNING_KEY_ID = UUID.fromString("7a4c2186-608e-43e4-8a95-5747461d3447");
    private static final String DIGEST = "dGhlIGRpZ2VzdA==";
    private static final String SIGNATURE = "YW4gZXhhbXBsZSBzaWduYXR1cmU=";

    @Mock
    private HttpSigner httpSigner;

    @Mock
    private Signer signer;

    @Mock
    private X509Certificate clientSigningCertificate;

    private StetSigningPaymentHttpHeadersFactory httpHeadersFactory;

    @BeforeEach
    void setUp() {
        httpHeadersFactory = new StetSigningPaymentHttpHeadersFactory(httpSigner, () -> LAST_EXTERNAL_TRACE_ID);
    }

    @Test
    void shouldCreateHttpHeadersForPaymentAccessToken() {
        // given
        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        StetTokenPaymentPreExecutionResult preExecutionResult = StetTokenPaymentPreExecutionResult.builder()
                .authMeans(DefaultAuthenticationMeans.builder()
                        .clientId(CLIENT_ID)
                        .clientSecret(CLIENT_SECRET)
                        .build())
                .build();

        // when
        HttpHeaders httpHeaders = httpHeadersFactory.createPaymentAccessTokenHttpHeaders(preExecutionResult, requestBody);

        // then
        assertThat(httpHeaders.toSingleValueMap())
                .hasSize(3)
                .containsEntry(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .containsEntry(HttpHeaders.AUTHORIZATION, BASIC_AUTHORIZATION)
                .containsEntry(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
    }

    @Test
    void shouldCreateSigningHttpHeadersForPaymentInitiation() {
        // given
        StetPaymentInitiationRequestDTO requestDTO = StetPaymentInitiationRequestDTO.builder()
                .build();

        StetInitiatePreExecutionResult preExecutionResult = StetInitiatePreExecutionResult.builder()
                .accessToken(ACCESS_TOKEN)
                .psuIpAddress(PSU_IP_ADDRESS)
                .authMeans(createAuthenticationMeans())
                .build();

        given(httpSigner.getDigest(any()))
                .willReturn(DIGEST);
        given(httpSigner.getSignature(any(HttpHeaders.class), any(SignatureData.class)))
                .willReturn(SIGNATURE);

        // when
        HttpHeaders httpHeaders = httpHeadersFactory.createPaymentInitiationHttpHeaders(preExecutionResult, requestDTO);

        // then
        assertThat(httpHeaders.toSingleValueMap()).satisfies(validateHttpHeaders());

        then(httpSigner)
                .should()
                .getDigest(requestDTO);
        then(httpSigner)
                .should()
                .getSignature(any(HttpHeaders.class), any(SignatureData.class));

    }

    @Test
    void shouldCreateSigningHttpHeadersForPaymentSubmit() {
        // given
        StetPaymentConfirmationRequestDTO requestDTO = StetPaymentConfirmationRequestDTO.builder()
                .build();

        StetConfirmationPreExecutionResult preExecutionResult = StetConfirmationPreExecutionResult.builder()
                .accessToken(ACCESS_TOKEN)
                .psuIpAddress(PSU_IP_ADDRESS)
                .authMeans(createAuthenticationMeans())
                .build();

        given(httpSigner.getDigest(any()))
                .willReturn(DIGEST);
        given(httpSigner.getSignature(any(HttpHeaders.class), any(SignatureData.class)))
                .willReturn(SIGNATURE);

        // when
        HttpHeaders httpHeaders = httpHeadersFactory.createPaymentSubmitHttpHeaders(preExecutionResult, requestDTO);

        // then
        assertThat(httpHeaders.toSingleValueMap()).satisfies(validateHttpHeaders());

        then(httpSigner)
                .should()
                .getDigest(requestDTO);
        then(httpSigner)
                .should()
                .getSignature(any(HttpHeaders.class), any(SignatureData.class));
    }

    @Test
    void shouldCreateSigningHttpHeadersForPaymentStatus() {
        // given
        Object requestBody = new byte[0];
        StetConfirmationPreExecutionResult preExecutionResult = StetConfirmationPreExecutionResult.builder()
                .accessToken(ACCESS_TOKEN)
                .psuIpAddress(PSU_IP_ADDRESS)
                .authMeans(createAuthenticationMeans())
                .build();

        given(httpSigner.getDigest(any()))
                .willReturn(DIGEST);
        given(httpSigner.getSignature(any(HttpHeaders.class), any(SignatureData.class)))
                .willReturn(SIGNATURE);

        // when
        HttpHeaders httpHeaders = httpHeadersFactory.createPaymentStatusHttpHeaders(preExecutionResult);

        // then
        assertThat(httpHeaders.toSingleValueMap()).satisfies(validateHttpHeaders());

        then(httpSigner)
                .should()
                .getDigest(requestBody);
        then(httpSigner)
                .should()
                .getSignature(any(HttpHeaders.class), any(SignatureData.class));
    }

    @Test
    void shouldPrepareCommonHttpHeaders() {
        // given
        Object requestBody = new byte[0];
        SignatureData signatureData = new SignatureData(
                signer,
                SIGNING_KEY_HEADER_ID,
                CLIENT_SIGNING_KEY_ID,
                clientSigningCertificate,
                HttpMethod.POST,
                PAYMENT_PATH);

        given(httpSigner.getDigest(any()))
                .willReturn(DIGEST);
        given(httpSigner.getSignature(any(HttpHeaders.class), any(SignatureData.class)))
                .willReturn(SIGNATURE);

        // when
        HttpHeaders httpHeaders = httpHeadersFactory.prepareCommonHttpHeaders(
                signatureData,
                ACCESS_TOKEN,
                PSU_IP_ADDRESS,
                requestBody);

        // then
        assertThat(httpHeaders.toSingleValueMap()).satisfies(validateHttpHeaders());

        then(httpSigner)
                .should()
                .getDigest(requestBody);
        then(httpSigner)
                .should()
                .getSignature(any(HttpHeaders.class), any(SignatureData.class));
    }

    private DefaultAuthenticationMeans createAuthenticationMeans() {
        return DefaultAuthenticationMeans.builder()
                .clientId(CLIENT_ID)
                .clientSecret(CLIENT_SECRET)
                .signingKeyIdHeader(SIGNING_KEY_HEADER_ID)
                .clientSigningKeyId(CLIENT_SIGNING_KEY_ID)
                .clientSigningCertificate(clientSigningCertificate)
                .build();
    }

    private Consumer<Map<String, String>> validateHttpHeaders() {
        return (httpHeaders) -> {
            assertThat(httpHeaders)
                    .hasSize(7)
                    .containsEntry(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .containsEntry(HttpHeaders.AUTHORIZATION, "Bearer " + ACCESS_TOKEN)
                    .containsEntry(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .containsEntry(HttpHeadersExtension.PSU_IP_ADDRESS, PSU_IP_ADDRESS)
                    .containsEntry(HttpHeadersExtension.X_REQUEST_ID, LAST_EXTERNAL_TRACE_ID)
                    .containsEntry(HttpHeadersExtension.DIGEST, DIGEST)
                    .containsEntry(HttpHeadersExtension.SIGNATURE, SIGNATURE);
        };
    }
}
