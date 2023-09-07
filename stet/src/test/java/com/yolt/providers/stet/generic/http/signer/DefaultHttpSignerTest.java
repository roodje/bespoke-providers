package com.yolt.providers.stet.generic.http.signer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.stet.generic.domain.DigestAlgorithm;
import com.yolt.providers.stet.generic.domain.SignatureData;
import com.yolt.providers.stet.generic.http.signer.signature.SignatureStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;

import java.security.cert.X509Certificate;
import java.util.StringJoiner;
import java.util.UUID;

import static com.yolt.providers.stet.generic.domain.HttpHeadersExtension.DIGEST;
import static com.yolt.providers.stet.generic.domain.HttpHeadersExtension.X_REQUEST_ID;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@ExtendWith(MockitoExtension.class)
class DefaultHttpSignerTest {

    private static final String HOST = "example.com";
    private static final String ENDPOINT = "/example";
    private static final String HEADER_KEY_ID = "http://example.com/";
    private static final UUID SIGNING_KEY_ID = UUID.fromString("c615eec5-7463-46d9-ae0d-de582c1c389a");

    @Mock
    private Signer signer;

    @Mock
    private X509Certificate signingCertificate;

    @Mock
    private SignatureStrategy signatureStrategy;

    private HttpSigner httpSigner;

    @BeforeEach
    void initialize() {
        httpSigner = new DefaultHttpSigner(signatureStrategy, new ObjectMapper(), DigestAlgorithm.SHA_256);
    }

    @Test
    void shouldComputeDigestForRequestBody() {
        // given
        String requestBody = "{\"field\":\"value\"}";

        // when
        String digest = httpSigner.getDigest(requestBody);

        // then
        assertThat(digest).isEqualTo("SHA-256=tWJ7csE4dZqy2mtDQGXm2zEVXDFt67Ij0w8kPXNBraU=");
    }

    @Test
    void shouldComputeDigestForEmptyRequestBody() {
        // given
        byte[] requestBody = new byte[0];

        // when
        String digest = httpSigner.getDigest(requestBody);

        // then
        assertThat(digest).isEqualTo("SHA-256=47DEQpj8HBSa+/TImW+5JCeuQeRkm5NMpJWZG3hSuFU=");
    }

    @Test
    void shouldComputeDigestForNullableRequestBody() {
        // given
        Object requestBody = null;

        // when
        String digest = httpSigner.getDigest(requestBody);

        // then
        assertThat(digest).isEqualTo("SHA-256=47DEQpj8HBSa+/TImW+5JCeuQeRkm5NMpJWZG3hSuFU=");
    }

    @Test
    void shouldComputeSignature() {
        // given
        SignatureData signatureData = createSignatureData();
        HttpHeaders headers = createHttpHeaders();

        String expectedSignature = new StringJoiner(", ")
                .add("keyId=\"http://example.com/\"")
                .add("algorithm=\"rsa-sha256\"")
                .add("headers=\"(request-target) content-type digest x-request-id\"")
                .add("signature=\"KHJlcXVlc3QtdGFyZ2V0KTogZ2V0IC9hY2NvdW50cwpjb250\"")
                .toString();

        when(signatureStrategy.getSignature(any(HttpHeaders.class), any(SignatureData.class)))
                .thenReturn(expectedSignature);

        // when
        String signature = httpSigner.getSignature(headers, signatureData);

        // then
        assertThat(signature).isEqualTo(expectedSignature);
    }

    private SignatureData createSignatureData() {
        return new SignatureData(signer, HEADER_KEY_ID, SIGNING_KEY_ID, signingCertificate, GET, HOST, ENDPOINT);
    }

    private HttpHeaders createHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("ad024aa4-a30f-4c88-97df-35998b574c4c");
        headers.setAccept(singletonList(APPLICATION_JSON));
        headers.setContentType(APPLICATION_JSON);
        headers.set(DIGEST, httpSigner.getDigest(new byte[0]));
        headers.set(X_REQUEST_ID, "cad807b4-e9ac-4afd-a56c-463ceca21fd1");
        return headers;
    }
}
