package com.yolt.providers.openbanking.ais.generic2.signer;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.lang.JoseException;

public interface UserRequestTokenSigner {

    String sign(DefaultAuthMeans authenticationMeans,
                JwtClaims claims,
                Signer signer) throws JoseException;

}
