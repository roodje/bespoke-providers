package com.yolt.providers.stet.generic.http.signer.signature;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.util.KeyUtil;
import com.yolt.providers.stet.generic.domain.SignatureData;
import com.yolt.securityutils.signing.SignatureAlgorithm;
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
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

import static com.yolt.providers.stet.generic.domain.HttpHeadersExtension.DIGEST;
import static com.yolt.providers.stet.generic.domain.HttpHeadersExtension.X_REQUEST_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.*;

@ExtendWith(MockitoExtension.class)
class EnhancedCavageSignatureStrategyTest {

    private static final String CERTIFICATE_PATH = "certificates/fake-certificate.pem";
    private static final String HEADER_KEY_ID = "http://example.com/cert";
    private static final UUID SIGNING_KEY_ID = UUID.fromString("c06e15f3-d24a-4493-832a-a28a13d36e1d");

    @Mock
    private Signer signer;

    private EnhancedCavageSignatureStrategy signatureStrategy;

    @BeforeEach
    void initialize() {
        signatureStrategy = new EnhancedCavageSignatureStrategy(SignatureAlgorithm.SHA256_WITH_RSA);
    }

    @Test
    void shouldReturnHeadersToSign() {
        // given-when
        List<String> headersToSign = signatureStrategy.getHeadersToSign();

        // then
        assertThat(headersToSign).containsExactlyElementsOf(List.of(HOST, X_REQUEST_ID, DIGEST));
    }

    @Test
    void shouldReturnSignatureWithHostSpecified() {
        // given
        HttpHeaders httpHeaders = createHttpHeaders();
        SignatureData signatureData = createSignatureData();

        when(signer.sign(any(byte[].class), any(UUID.class), any(SignatureAlgorithm.class)))
                .thenReturn("KHJlcXXlc3QtdGFyZ2V0KTogZ2V0IC9hY2NvdW50cwpjb250");

        // when
        String signature = signatureStrategy.getSignature(httpHeaders, signatureData);

        // then
        assertThat(signature).matches(Pattern.compile(
                "keyId=\"http://example\\.com/cert\"," +
                "algorithm=\"rsa-sha256\"," +
                "headers=\"\\(request-target\\) \\(created\\) \\(expires\\) host x-request-id digest\"," +
                "\\(created\\)=\\d{10,12}," +
                "\\(expires\\)=\\d{10,12}," +
                "signature=\"KHJlcXXlc3QtdGFyZ2V0KTogZ2V0IC9hY2NvdW50cwpjb250\""));
    }

    private HttpHeaders createHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HOST, "www.example.com");
        headers.set(DATE, "2020-01-01T10:00:00+01:00");
        headers.setBearerAuth("dfdf9eac-d24b-4543-84ae-6db2af4a93bd");
        headers.set(CONTENT_TYPE, "application/json");
        headers.set(X_REQUEST_ID, "dfdf9eac-d24b-4543-84ae-6db2af4a93bd");
        headers.set(DIGEST, "ZGZkZjllYWMtZDI0Yi00NTQzLTg0YWUtNmRiMmFmNGE5M2Jk");
        return headers;
    }

    private SignatureData createSignatureData() {
        return new SignatureData(signer, HEADER_KEY_ID, SIGNING_KEY_ID, readCertificate(), HttpMethod.GET, "localhost", "/example");
    }

    @SneakyThrows
    private X509Certificate readCertificate() {
        URL certificateUrl = this.getClass().getClassLoader().getResource(CERTIFICATE_PATH);
        String certificatePem = new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(certificateUrl).toURI())));
        return KeyUtil.createCertificateFromPemFormat(certificatePem);
    }
}
