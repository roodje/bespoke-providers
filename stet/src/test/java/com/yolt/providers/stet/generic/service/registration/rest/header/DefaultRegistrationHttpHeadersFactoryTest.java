package com.yolt.providers.stet.generic.service.registration.rest.header;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.domain.HttpHeadersExtension;
import com.yolt.providers.stet.generic.domain.SignatureData;
import com.yolt.providers.stet.generic.service.registration.request.RegistrationRequest;
import com.yolt.providers.stet.generic.http.signer.HttpSigner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DefaultRegistrationHttpHeadersFactoryTest {

    @InjectMocks
    private DefaultRegistrationHttpHeadersFactory sut;

    @Mock
    private HttpSigner httpSigner;

    @Mock
    private Signer signer;

    @Mock
    private DefaultAuthenticationMeans authMeans;

    @Captor
    private ArgumentCaptor<SignatureData> signatureDataArgumentCaptor;

    @Test
    void shouldReturnProperSetOfHttpHeadersForCreateRegistrationHttpHeaders() {
        // given
        HttpHeaders expectedHeaders = new HttpHeaders();
        expectedHeaders.setContentType(MediaType.APPLICATION_JSON);
        expectedHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        expectedHeaders.set(HttpHeadersExtension.X_REQUEST_ID, "1");
        expectedHeaders.set(HttpHeadersExtension.DIGEST, "digest");
        expectedHeaders.set(HttpHeadersExtension.SIGNATURE, "signature");
        when(authMeans.getSigningKeyIdHeader())
                .thenReturn("sighingKeyIdHeader");
        RegistrationRequest registrationRequest = new RegistrationRequest(
                authMeans,
                signer,
                () -> "1",
                "redirectUrl",
                "provider"
        );
        UUID signingKeyId = UUID.randomUUID();
        when(authMeans.getClientSigningKeyId())
                .thenReturn(signingKeyId);
        X509Certificate signingCertificate = mock(X509Certificate.class);
        when(authMeans.getClientSigningCertificate())
                .thenReturn(signingCertificate);
        when(httpSigner.getDigest(any(Object.class)))
                .thenReturn("digest");
        when(httpSigner.getSignature(any(HttpHeaders.class), any(SignatureData.class)))
                .thenReturn("signature");

        // when
        HttpHeaders result = sut.createRegistrationHttpHeaders(registrationRequest, "body", HttpMethod.POST, "http://localhost/register");

        // then
        assertThat(result).containsExactlyInAnyOrderEntriesOf(expectedHeaders);
        verify(httpSigner).getDigest("body");
        verify(httpSigner).getSignature(eq(expectedHeaders), signatureDataArgumentCaptor.capture());
        SignatureData capturedSignatureData = signatureDataArgumentCaptor.getValue();
        assertThat(capturedSignatureData).extracting(
                SignatureData::getSigner,
                SignatureData::getHeaderKeyId,
                SignatureData::getSigningKeyId,
                SignatureData::getSigningCertificate,
                SignatureData::getHttpMethod,
                SignatureData::getHost,
                SignatureData::getEndpoint
        ).contains(signer, "sighingKeyIdHeader", signingKeyId, signingCertificate, HttpMethod.POST, "localhost", "/register");
    }
}
