package com.yolt.providers.yoltprovider.pis.sepa;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.encoders.Base64;

import javax.validation.constraints.NotNull;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SigningUtils {

    static final SignatureAlgorithm SIGNATURE_ALGORITHM = SignatureAlgorithm.SHA256_WITH_RSA;

    private static final String SHA_256_ALGORITHM = "SHA-256";

    public static String prepareDigest(@NotNull final byte[] bytes) {
        try {
            return SHA_256_ALGORITHM + "=" + Base64.toBase64String(MessageDigest.getInstance(SHA_256_ALGORITHM).digest(bytes));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("Incorrect digest algorithm '" + SHA_256_ALGORITHM + "' was used", e);
        }
    }

    public static String prepareSignature(@NotNull final UUID clientId, @NotNull final String digest,
                                   @NotNull final Signer signer, @NotNull final UUID privateKid) {
        final String signingString = "clientId: " + clientId.toString() + ", digest: " + digest;
        return signer.sign(signingString.getBytes(), privateKid, SIGNATURE_ALGORITHM);
    }
}
