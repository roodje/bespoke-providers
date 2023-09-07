package com.yolt.providers.dkbgroup;

import com.yolt.providers.common.cryptography.JwsSigningResult;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.util.KeyUtil;
import com.yolt.providers.dkbgroup.common.auth.DKBGroupAuthMeans;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import jdk.jshell.spi.ExecutionControl;
import lombok.SneakyThrows;
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

    private static final String PRIVATE_KEY_ID_NAME = "certificates/fake-private_key.pem";

    private final PrivateKey privateKey;

    public SignerMock() {
        this.privateKey = createKey(readFile(PRIVATE_KEY_ID_NAME));
    }

    @Override
    public JwsSigningResult sign(final JsonWebSignature jws,
                                 final UUID privateKid,
                                 final SignatureAlgorithm signatureAlgorithm) {
        jws.setKey(privateKey);
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

    @SneakyThrows
    @Override
    public String sign(final byte[] bytesToSign,
                       final UUID privateKid,
                       final SignatureAlgorithm signatureAlgorithm) {
        throw new ExecutionControl.NotImplementedException("Operation not supported.");
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
            URI fileURI = DKBGroupAuthMeans.class
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
