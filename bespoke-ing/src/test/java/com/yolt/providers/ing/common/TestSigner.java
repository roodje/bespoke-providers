package com.yolt.providers.ing.common;

import com.yolt.providers.common.cryptography.JwsSigningResult;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.bouncycastle.util.encoders.Base64;
import org.jose4j.jws.JsonWebSignature;

import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.Signature;
import java.util.UUID;

@RequiredArgsConstructor
public class TestSigner implements Signer {

    private static final UUID TEST_SIGNING_KEY_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private final PrivateKey signingKey;

    @Override
    public JwsSigningResult sign(final JsonWebSignature jws, final UUID privateKid, final SignatureAlgorithm signatureAlgorithm) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String sign(final byte[] bytesToSign, final UUID privateKid, final SignatureAlgorithm signatureAlgorithm) {
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
            throw new RuntimeException("Could not create the request signature for the http request on ING Netherlands.");
        }
    }
}