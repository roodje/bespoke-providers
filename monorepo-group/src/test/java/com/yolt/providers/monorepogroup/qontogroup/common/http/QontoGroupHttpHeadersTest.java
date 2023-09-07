package com.yolt.providers.monorepogroup.qontogroup.common.http;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.monorepogroup.qontogroup.common.QontoGroupAuthenticationMeans;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;


@ExtendWith(MockitoExtension.class)
class QontoGroupHttpHeadersTest {

    @Mock
    private Signer signer;

    @Test
    void shouldSignHeaders() {
        //given
        var privateKeyId = UUID.randomUUID();
        var certificateUrl = "https://s3baseurl.com/some-fingerprint.pem";
        QontoGroupHttpHeaders headers = new QontoGroupHttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("PSU-IP-Address", "127.0.0.1");
        given(signer.sign(any(byte[].class), eq(privateKeyId), eq(SignatureAlgorithm.SHA256_WITH_RSA)))
                .willReturn("calculated signature");
        var expectedDigest = "SHA-256=47DEQpj8HBSa+/TImW+5JCeuQeRkm5NMpJWZG3hSuFU=";
        var expectedSignature = "keyId=\"https://s3baseurl.com/some-fingerprint.pem\",algorithm=\"rsa-sha256\",headers=\"content-type psu-ip-address digest\",signature=\"calculated signature\"";
        //when
        headers.sign(new QontoGroupAuthenticationMeans.SigningData(certificateUrl, privateKeyId), signer);

        //then
        assertThat(headers.toSingleValueMap()).containsAllEntriesOf(Map.of(
                "Content-Type", MediaType.APPLICATION_JSON.toString(),
                "PSU-IP-Address", "127.0.0.1",
                "Digest", expectedDigest,
                "Signature", expectedSignature
        ));
    }
}