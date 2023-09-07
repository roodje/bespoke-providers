package com.yolt.providers.openbanking.ais.barclaysgroup.common.signer;

import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.signer.ExternalUserRequestTokenSigner;
import org.jose4j.jws.JsonWebSignature;

public class BarclaysGroupUserRequestTokenSignerV2 extends ExternalUserRequestTokenSigner {

    public BarclaysGroupUserRequestTokenSignerV2(String jwsAlgorithm) {
        super(jwsAlgorithm);
    }

    @Override
    protected void adjustJWSHook(JsonWebSignature jws, DefaultAuthMeans authMeans) {
        jws.getHeaders().setObjectHeaderValue("typ", "JWT");
    }
}
