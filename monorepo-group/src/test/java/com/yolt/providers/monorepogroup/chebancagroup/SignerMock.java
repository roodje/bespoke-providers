package com.yolt.providers.monorepogroup.chebancagroup;

import com.yolt.providers.common.cryptography.JwsSigningResult;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.util.KeyUtil;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import org.apache.commons.lang3.NotImplementedException;
import org.jose4j.jws.JsonWebSignature;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.UUID;

public class SignerMock implements Signer {

    @Override
    public JwsSigningResult sign(final JsonWebSignature jws, final UUID privateKid, final SignatureAlgorithm signatureAlgorithm) {
        throw new NotImplementedException();
    }

    @Override
    public String sign(final byte[] bytesToSign, final UUID privateKid, final SignatureAlgorithm signatureAlgorithm) {
        Signature signer;
        try {
            signer = Signature.getInstance("SHA256WithRSA");
            String privateKeyFileName = "certificates/fake-private_key.pem";
            signer.initSign(createKey(readFile(privateKeyFileName)));
            signer.update(bytesToSign);

            Base64.Encoder b64encoder = Base64.getEncoder();
            return b64encoder.encodeToString(signer.sign());
        } catch (NoSuchAlgorithmException | InvalidKeyException  | SignatureException e) {
            throw new RuntimeException(e);
        }
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
