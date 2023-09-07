package com.yolt.providers.openbanking.ais.newdaygroup.common.signer;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.signer.UserRequestTokenSigner;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;

@RequiredArgsConstructor
public class NewDayUserRequestTokenSigner implements UserRequestTokenSigner {

    private final String jwsAlgorithm;

    @Override
    public String sign(DefaultAuthMeans authenticationMeans, JwtClaims claims, Signer signer) {
        JsonWebSignature jws = new JsonWebSignature();
        jws.setAlgorithmHeaderValue(jwsAlgorithm);
        jws.setKeyIdHeaderValue(authenticationMeans.getCertificateId());
        jws.setPayload(claims.toJson());
        return signer.sign(jws,
                authenticationMeans.getSigningPrivateKeyId(),
                SignatureAlgorithm.findByJsonSignatureAlgorithmOrThrowException(jwsAlgorithm))
                .getCompactSerialization();
    }
}
