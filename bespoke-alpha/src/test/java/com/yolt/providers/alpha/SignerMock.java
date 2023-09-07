package com.yolt.providers.alpha;

import com.yolt.providers.common.cryptography.JwsSigningResult;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import jdk.jshell.spi.ExecutionControl;
import lombok.SneakyThrows;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.lang.JoseException;

import java.security.PrivateKey;
import java.util.UUID;

public class SignerMock implements Signer {

    private static final String PRIVATE_KEY_ID_NAME = "certificates/fake-private_key.pem";

    private final PrivateKey privateKey;

    public SignerMock() {
        this.privateKey = TestUtil.createKey(TestUtil.readFile(PRIVATE_KEY_ID_NAME));
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
}
