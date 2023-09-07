package com.yolt.providers.yoltprovider.pis.sepa;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import org.bouncycastle.util.encoders.Base64;
import org.junit.jupiter.api.Test;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class SigningUtilsTest {

    @Test
    public void shouldReturnCorrectDigestForPrepareDigestWithCorrectData() throws NoSuchAlgorithmException {
        // given
        String message = "some message";
        String expected = "SHA-256=" + Base64.toBase64String(MessageDigest.getInstance("SHA-256").digest(message.getBytes()));

        // when
        String digest = SigningUtils.prepareDigest(message.getBytes());

        // then
        assertThat(digest).isEqualTo(expected);
    }

    @Test
    public void shouldPassProperSigningStringForPrepareSignatureWithCorrectData() {
        // given
        Signer signerMock = mock(Signer.class);
        UUID clientId = UUID.randomUUID();
        String digest = "some digest";
        UUID kid = UUID.randomUUID();
        String signingString = "clientId: " + clientId.toString() + ", digest: " + digest;
        String expectedSignature = "signature";
        when(signerMock.sign(any(byte[].class), any(UUID.class), any(SignatureAlgorithm.class)))
                .thenReturn(expectedSignature);

        // when
        String result = SigningUtils.prepareSignature(clientId, digest, signerMock, kid);

        // then
        verify(signerMock).sign(signingString.getBytes(), kid, SigningUtils.SIGNATURE_ALGORITHM);
        assertThat(result).isEqualTo(expectedSignature);
    }
}
