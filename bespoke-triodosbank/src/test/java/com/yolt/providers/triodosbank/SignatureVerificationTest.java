package com.yolt.providers.triodosbank;


import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.triodosbank.common.model.domain.SignatureData;
import com.yolt.providers.triodosbank.common.util.TriodosBankSigningUtil;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import javax.security.auth.x500.X500Principal;
import java.math.BigInteger;
import java.security.cert.X509Certificate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SignatureVerificationTest {

    @Test
    public void shouldCalculateDigestCorrectly() {
        // given
        String expectedDigest = "SHA-256=X48E9qOokqqrvdts8nOJRJN3OWDUoyWxBf7kbu9DBPE=";
        String body = "{\"hello\": \"world\"}";

        // when
        String actualDigest = TriodosBankSigningUtil.getDigest(body.getBytes());

        // then
        assertThat(actualDigest).isEqualTo(expectedDigest);
    }

    @Test
    public void shouldConstructSignatureCorrectly() {
        // given
        var mockedCertificate = mock(X509Certificate.class);
        var signingKeyId = UUID.randomUUID();
        var mockSigner = mock(Signer.class);
        var name = "C=ES, O=\"Entrust Datacard Europe, S.L.\", OID.2.5.4.97=VATES-B81188047, CN=Entrust Certification Authority - ES QSeal1";
        var x500Principal = new X500Principal(name);
        when(mockSigner.sign(any(byte[].class), any(), any())).thenReturn("SIGNED_SIGNATURE");
        when(mockedCertificate.getIssuerX500Principal()).thenReturn(x500Principal);
        when(mockedCertificate.getSerialNumber()).thenReturn(new BigInteger("7454ac40011057b2f7532176c3297f37",16));
        var signatureData = SignatureData.builder()
                .signer(mockSigner)
                .signingCertificate(mockedCertificate)
                .signingKeyId(signingKeyId)
                .build();
        var httpHeaders = new HttpHeaders();
        httpHeaders.add("digest", "SHA-256=47DEQpj8HBSa+/TImW+5JCeuQeRkm5NMpJWZG3hSuFU=");
        httpHeaders.add("x-request-id", "175067b2-2bda-4d6e-9176-550e6d04d1a0");

        // when
        String actualDigest = TriodosBankSigningUtil.getSignature(httpHeaders, signatureData);

        // then
        assertThat(actualDigest).isEqualTo("keyId=\"SN=7454ac40011057b2f7532176c3297f37,CA=C=ES, O=\"Entrust Datacard Europe, S.L.\", OID.2.5.4.97=VATES-B81188047, CN=Entrust Certification Authority - ES QSeal1\",algorithm=\"rsa-sha256\",headers=\"digest x-request-id\",signature=\"SIGNED_SIGNATURE\"");
    }
}
