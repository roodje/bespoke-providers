package com.yolt.providers.openbanking.ais.kbciegroup.common.oauth2.token;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.signer.UserRequestTokenSigner;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.keys.X509Util;
import org.jose4j.lang.JoseException;

@RequiredArgsConstructor
public class KbcIeGroupUserTokenSigner implements UserRequestTokenSigner {

    public static final String TYP_CLAIM_NAME = "typ";
    public static final String TYP_CLAIM_VALUE = "JWT";
    private final String jwsAlgorithm;

    @Override
    public String sign(DefaultAuthMeans authenticationMeans, JwtClaims claims, Signer signer) throws JoseException {
        JsonWebSignature jws = new JsonWebSignature();
        jws.setHeader(TYP_CLAIM_NAME, TYP_CLAIM_VALUE);
        jws.setAlgorithmHeaderValue(jwsAlgorithm);
        jws.setKeyIdHeaderValue(X509Util.x5t(authenticationMeans.getSigningCertificate()));
        jws.setPayload(claims.toJson());
        return signer.sign(jws, authenticationMeans.getSigningPrivateKeyId(), SignatureAlgorithm.findByJsonSignatureAlgorithmOrThrowException(jwsAlgorithm)).getCompactSerialization();
    }
}
