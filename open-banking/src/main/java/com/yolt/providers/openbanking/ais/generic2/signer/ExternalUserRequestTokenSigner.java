package com.yolt.providers.openbanking.ais.generic2.signer;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;

/**
 * Implementation that uses external signer.
 * Use only if private key is migrated to HSM.
 */
@RequiredArgsConstructor
public class ExternalUserRequestTokenSigner implements UserRequestTokenSigner {

    private final String jwsAlgorithm;

    @Override
    public String sign(DefaultAuthMeans authenticationMeans, JwtClaims claims, Signer signer) {
        JsonWebSignature jws = new JsonWebSignature();
        jws.setAlgorithmHeaderValue(jwsAlgorithm);
        jws.setKeyIdHeaderValue(authenticationMeans.getSigningKeyIdHeader());
        jws.setPayload(claims.toJson());

        adjustJWSHook(jws, authenticationMeans);

        return signer.sign(jws, authenticationMeans.getSigningPrivateKeyId(), SignatureAlgorithm.findByJsonSignatureAlgorithmOrThrowException(jwsAlgorithm)).getCompactSerialization();
    }

    protected void adjustJWSHook(JsonWebSignature jws, DefaultAuthMeans authenticationMeans) {
        //This method allows to change JWS in extending classes with bank specific changes
    }
}
