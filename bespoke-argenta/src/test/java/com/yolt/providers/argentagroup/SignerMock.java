package com.yolt.providers.argentagroup;

import com.yolt.providers.common.cryptography.JwsSigningResult;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import org.jose4j.jws.JsonWebSignature;

import java.util.UUID;

public class SignerMock implements Signer {

    @Override
    public JwsSigningResult sign(final JsonWebSignature jws,
                                 final UUID privateKid,
                                 final SignatureAlgorithm signatureAlgorithm) {
        return new JwsSigningResult() {
            @Override
            public String getCompactSerialization() {
                return "xx.xxx.xx";
            }

            @Override
            public String getDetachedContentCompactSerialization() {
                return "xx..xx";
            }
        };
    }

    @Override
    public String sign(final byte[] bytesToSign,
                       final UUID privateKid,
                       final SignatureAlgorithm signatureAlgorithm) {
        return UUID.randomUUID().toString();
    }
}
