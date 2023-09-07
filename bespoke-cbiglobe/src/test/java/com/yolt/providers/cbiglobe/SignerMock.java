package com.yolt.providers.cbiglobe;

import com.yolt.providers.common.cryptography.JwsSigningResult;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.util.KeyUtil;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.bouncycastle.util.encoders.Base64;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.lang.JoseException;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.util.UUID;

@Component
@AllArgsConstructor
@NoArgsConstructor
public class SignerMock implements Signer {

    private String privateKeyFileName = "certificates/fake-private_key.pem";

    @Override
    public JwsSigningResult sign(final JsonWebSignature jws,
                                 final UUID privateKid,
                                 final SignatureAlgorithm signatureAlgorithm) {
        jws.setKey(createKey(readFile(privateKeyFileName)));
        return new JwsSigningResult() {
            @Override
            public String getCompactSerialization() {
                try {
                    return jws.getCompactSerialization();
                } catch (JoseException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public String getDetachedContentCompactSerialization() {
                try {
                    return jws.getDetachedContentCompactSerialization();
                } catch (JoseException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    @Override
    public String sign(final byte[] bytesToSign,
                       final UUID privateKid,
                       final SignatureAlgorithm signatureAlgorithm) {
        return new String(Base64.encode(bytesToSign)).replaceAll("\n", "");
    }

    private static PrivateKey createKey(final String keyString) {
        try {
            return KeyUtil.createPrivateKeyFromPemFormat(keyString);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Error creating private key");
        }
    }

    private static String readFile(final String filename) {
        try {
            URI fileURI = SignerMock.class
                    .getClassLoader()
                    .getResource(filename)
                    .toURI();

            Path filePath = new File(fileURI).toPath();
            return String.join("\n", Files.readAllLines(filePath, StandardCharsets.UTF_8));
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException("Error reading private key");
        }
    }
}
