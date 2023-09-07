package com.yolt.providers.stet;

import com.yolt.providers.common.cryptography.JwsSigningResult;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.util.KeyUtil;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import org.bouncycastle.util.encoders.Base64;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.lang.JoseException;

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

public class SignerMock implements Signer {

    private String privateKeyFileName = "certificates/fake-private-key.pem";

    @Override
    public String sign(byte[] bytesToSign, UUID privateKid, SignatureAlgorithm signatureAlgorithm) {
        return new String(Base64.encode(bytesToSign)).replaceAll("\n", "");
    }

    @Override
    public JwsSigningResult sign(JsonWebSignature jws, UUID privateKid, SignatureAlgorithm signatureAlgorithm) {
        jws.setKey(createKey(readFile(privateKeyFileName)));
        return new JwsSigningResult() {
            @Override
            public String getCompactSerialization() {
                try {
                    return jws.getCompactSerialization();
                } catch (JoseException e) {
                    throw new IllegalStateException(e);
                }
            }

            @Override
            public String getDetachedContentCompactSerialization() {
                try {
                    return jws.getDetachedContentCompactSerialization();
                } catch (JoseException e) {
                    throw new IllegalStateException(e);
                }
            }
        };
    }

    private static PrivateKey createKey(final String keyString) {
        try {
            return KeyUtil.createPrivateKeyFromPemFormat(keyString);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("Error creating private key");
        }
    }

    private static String readFile(final String filename) {
        try {
            URI fileURI = SignerMock.class.getClassLoader().getResource(filename).toURI();
            Path filePath = new File(fileURI).toPath();
            return String.join("\n", Files.readAllLines(filePath, StandardCharsets.UTF_8));
        } catch (IOException | URISyntaxException e) {
            throw new IllegalStateException("Error reading private key");
        }
    }
}
