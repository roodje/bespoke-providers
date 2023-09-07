package com.yolt.providers.openbanking.ais.capitalonegroup.common.oauth2;

import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.signer.ExternalUserRequestTokenSigner;
import org.jose4j.jws.JsonWebSignature;

public class CapitalOneUserRequestTokenSignerV2 extends ExternalUserRequestTokenSigner {

    public CapitalOneUserRequestTokenSignerV2(String jwsAlgorithm) {
        super(jwsAlgorithm);
    }

    @Override
    protected void adjustJWSHook(JsonWebSignature jws, DefaultAuthMeans authenticationMeans) {
        jws.getHeaders().setObjectHeaderValue("typ", "JWT");
    }
}
