package com.yolt.providers.alpha.common.auth;

import com.yolt.providers.alpha.common.auth.dto.AlphaAuthMeans;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;

import java.time.Clock;
import java.time.Instant;

@RequiredArgsConstructor
public class AlphaSigner {

    public static final String IAT = "iat";
    private static final String SIGNING_ALGORITHM = AlgorithmIdentifiers.RSA_USING_SHA256;
    private final Clock clock;

    public String getSignature(final String body,
                               final AlphaAuthMeans authMeans,
                               final Signer signer) {
        JsonWebSignature jws = new JsonWebSignature();
        jws.setAlgorithmHeaderValue(SIGNING_ALGORITHM);
        jws.setPayload(body);
        jws.setKeyIdHeaderValue(authMeans.getSigningKeyIdHeader());
        jws.setHeader(IAT, Long.toString(Instant.now(clock).getEpochSecond()));
        return signer.sign(jws, authMeans.getSigningPrivateKeyId(), SignatureAlgorithm.findByJsonSignatureAlgorithmOrThrowException(SIGNING_ALGORITHM))
                .getDetachedContentCompactSerialization();
    }
}
