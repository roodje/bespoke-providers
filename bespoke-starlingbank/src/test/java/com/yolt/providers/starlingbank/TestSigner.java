package com.yolt.providers.starlingbank;

import com.yolt.providers.common.cryptography.JwsSigningResult;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.util.KeyUtil;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.bouncycastle.util.encoders.Base64;
import org.jose4j.jws.JsonWebSignature;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.InvalidKeySpecException;
import java.util.Objects;
import java.util.UUID;

@RequiredArgsConstructor
public class TestSigner implements Signer {

    private static final String PRIVATE_KEY_FILE_NAME = "starlingbank/certificates/fake-private_key.pem";

    @Override
    public JwsSigningResult sign(final JsonWebSignature jws, final UUID privateKid, final SignatureAlgorithm signatureAlgorithm) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String sign(final byte[] bytesToSign, final UUID privateKid, final SignatureAlgorithm signatureAlgorithm) {
        PrivateKey signingKey = createKey(readFile());
        try {
            Signature signature = Signature.getInstance(signatureAlgorithm.getJvmAlgorithm());
            signature.initSign(signingKey);
            signature.update(bytesToSign);
            byte[] shaSignature = signature.sign();
            return Base64.toBase64String(shaSignature);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Could not create the request signature for the http request.");
        }
    }

    private static PrivateKey createKey(final String keyString) {
        try {
            return KeyUtil.createPrivateKeyFromPemFormat(keyString);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("Error creating private key");
        }
    }

    private static String readFile() {
        try {
            URI fileURI = Objects.requireNonNull(SampleAuthenticationMeans.class
                    .getClassLoader()
                    .getResource(PRIVATE_KEY_FILE_NAME))
                    .toURI();

            Path filePath = new File(fileURI).toPath();
            return String.join("\n", Files.readAllLines(filePath, StandardCharsets.UTF_8));
        } catch (IOException | URISyntaxException e) {
            throw new IllegalStateException("Error reading private key");
        }
    }
}