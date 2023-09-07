package com.yolt.providers.monorepogroup;

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
import java.util.UUID;

@RequiredArgsConstructor
public class TestSigner implements Signer {

    private static final UUID TEST_SIGNING_KEY_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private final PrivateKey signingKey;

    public TestSigner() {
        try {
            this.signingKey = KeyUtil.createPrivateKeyFromPemFormat((loadKey()));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException | URISyntaxException e) {
            throw new RuntimeException("Error while loading signing key");
        }
    }

    @Override
    public JwsSigningResult sign(JsonWebSignature jws, UUID privateKid, SignatureAlgorithm signatureAlgorithm) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String sign(byte[] bytesToSign, UUID privateKid, SignatureAlgorithm signatureAlgorithm) {
        if (!TEST_SIGNING_KEY_ID.equals(privateKid)) {
            throw new RuntimeException("Private Key with Id: " + privateKid.toString() + " does not exists");
        }
        try {
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(signingKey);
            signature.update(bytesToSign);
            byte[] shaSignature = signature.sign();
            return Base64.toBase64String(shaSignature);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("Could not create the request signature for the http request.");
        }
    }

    private String loadKey() throws IOException, URISyntaxException {
        URI uri = TestSigner.class
                .getClassLoader()
                .getResource("certificates/fake-private-key.key")
                .toURI();
        Path filePath = new File(uri).toPath();
        return String.join("\n", Files.readAllLines(filePath, StandardCharsets.UTF_8));
    }
}