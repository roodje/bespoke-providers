package com.yolt.providers.monorepogroup.atruviagroup.common.dto.internal;

import com.yolt.providers.common.exception.FormDecryptionFailedException;
import lombok.NonNull;
import org.jose4j.jwe.JsonWebEncryption;
import org.jose4j.lang.JoseException;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

public record AtruviaFormDecryptor(@NonNull String algorithm, @NonNull String encryption, @NonNull String privateKey) {

    public String decryptJwe(String jwe) {
        try {
            var spec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKey));
            var kf = KeyFactory.getInstance("RSA");
            var key = kf.generatePrivate(spec);
            var receiverJwe = new JsonWebEncryption();
            receiverJwe.setCompactSerialization(jwe);
            receiverJwe.setKey(key);
            checkTheAlgorithm(receiverJwe);
            return receiverJwe.getPlaintextString();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | JoseException e) {
            throw new FormDecryptionFailedException("Couldn't decrypt. Possible source of the issue: " + e.getClass().getSimpleName(), e);
        }
    }

    private void checkTheAlgorithm(JsonWebEncryption jwe) {
        if (!algorithm.equals(jwe.getAlgorithmHeaderValue())) {
            throw new FormDecryptionFailedException("Expected algorithm to be " + algorithm);
        }

        if (!encryption.equals(jwe.getEncryptionMethodHeaderParameter())) {
            throw new FormDecryptionFailedException("Expected encryption to be " + encryption);
        }
    }
}
