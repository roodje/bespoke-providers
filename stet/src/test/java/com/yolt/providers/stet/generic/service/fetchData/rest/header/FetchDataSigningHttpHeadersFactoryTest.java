package com.yolt.providers.stet.generic.service.fetchData.rest.header;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.util.KeyUtil;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.domain.SignatureData;
import com.yolt.providers.stet.generic.http.signer.HttpSigner;
import com.yolt.providers.stet.generic.service.fetchdata.request.DataRequest;
import com.yolt.providers.stet.generic.service.fetchdata.rest.header.FetchDataSigningHttpHeadersFactory;
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

import static com.yolt.providers.stet.generic.domain.HttpHeadersExtension.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.cloud.contract.spec.internal.MediaTypes.APPLICATION_JSON;
import static org.springframework.http.HttpHeaders.*;

@ExtendWith(MockitoExtension.class)
class FetchDataSigningHttpHeadersFactoryTest {

    private static final String CERTIFICATE_PATH = "certificates/fake-certificate.pem";
    private static final String ACCESS_TOKEN = "b6b4ffe8-5e60-4ef9-858f-78b48c5d7b11";
    private static final String LAST_EXTERNAL_TRACE_ID = "8bf52479-f18c-40cd-a1f3-3e667015f05a";
    private static final String DIGEST_VALUE = "285d0583-6965-43b7-8aa9-ffbe54cf5b94";
    private static final String SIGNATURE_VALUE = "7b8e49e3-a497-49a6-875f-fcfb2a4405d5";
    private static final String PSU_IP_ADDRESS_VALUE = "127.0.0.1";

    @Mock
    private Signer signer;

    @Mock
    private HttpSigner httpSigner;

    private FetchDataSigningHttpHeadersFactory httpHeadersFactory;

    @BeforeEach
    void initialize() {
        Supplier<String> lastExternalTraceIdSupplier = () -> LAST_EXTERNAL_TRACE_ID;
        httpHeadersFactory = new FetchDataSigningHttpHeadersFactory(httpSigner, lastExternalTraceIdSupplier);
    }

    @Test
    void shouldReturnSpecifiedSigningHeadersForFetchingData() {
        // given
        String endpoint = "/accounts";
        DefaultAuthenticationMeans authMeans = createAuthenticationMeans();
        DataRequest dataRequest = createDataRequest(authMeans);
        SignatureData signatureData = createSignatureData(endpoint, authMeans);

        when(httpSigner.getDigest(any()))
                .thenReturn(DIGEST_VALUE);
        when(httpSigner.getSignature(any(HttpHeaders.class), any(SignatureData.class)))
                .thenReturn(SIGNATURE_VALUE);

        // when
        HttpHeaders headers = httpHeadersFactory.createFetchDataHeaders(endpoint, dataRequest, HttpMethod.GET);

        // then
        Map<String, String> headersMap = headers.toSingleValueMap();
        assertThat(headersMap).hasSize(7);
        assertThat(headersMap).containsKey(ACCEPT).containsValue(APPLICATION_JSON);
        assertThat(headersMap).containsKey(AUTHORIZATION).containsValue("Bearer " + ACCESS_TOKEN);
        assertThat(headersMap).containsKey(CONTENT_TYPE).containsValue(APPLICATION_JSON);
        assertThat(headersMap).containsKey(PSU_IP_ADDRESS).containsValue(PSU_IP_ADDRESS_VALUE);
        assertThat(headersMap).containsKey(X_REQUEST_ID).containsValue(LAST_EXTERNAL_TRACE_ID);
        assertThat(headersMap).containsKey(DIGEST).containsValue(DIGEST_VALUE);
        assertThat(headersMap).containsKey(SIGNATURE).containsValue(SIGNATURE_VALUE);

        verify(httpSigner).getDigest(new byte[0]);
        verify(httpSigner).getSignature(headers, signatureData);
    }

    private DataRequest createDataRequest(DefaultAuthenticationMeans authMeans) {
        return new DataRequest("http://localhost", signer, authMeans, ACCESS_TOKEN, PSU_IP_ADDRESS_VALUE, false);
    }

    private DefaultAuthenticationMeans createAuthenticationMeans() {
        return DefaultAuthenticationMeans.builder()
                .signingKeyIdHeader("HeaderKeyId")
                .clientSigningKeyId(UUID.randomUUID())
                .clientSigningCertificate(readCertificate())
                .build();
    }

    private SignatureData createSignatureData(String endpoint, DefaultAuthenticationMeans authMeans) {
        return new SignatureData(
                signer,
                authMeans.getSigningKeyIdHeader(),
                authMeans.getClientSigningKeyId(),
                authMeans.getClientSigningCertificate(),
                HttpMethod.GET,
                "localhost",
                endpoint);
    }

    @SneakyThrows
    private X509Certificate readCertificate() {
        URL certificateUrl = this.getClass().getClassLoader().getResource(CERTIFICATE_PATH);
        String certificatePem = new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(certificateUrl).toURI())));
        return KeyUtil.createCertificateFromPemFormat(certificatePem);
    }
}
