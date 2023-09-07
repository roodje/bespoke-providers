package com.yolt.providers.openbanking.ais.barclaysgroup.common.signer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.signer.ExternalPaymentNoB64RequestSigner;
import org.jose4j.jws.JsonWebSignature;

public class BarclaysGroupPaymentRequestSignerV3 extends ExternalPaymentNoB64RequestSigner {

    public BarclaysGroupPaymentRequestSignerV3(final ObjectMapper objectMapper, final String jwsAlgorithm) {
        super(objectMapper, jwsAlgorithm);
    }

    @Override
    public void adjustJWSHook(final JsonWebSignature jws, DefaultAuthMeans authMeans) {
        if (jwsDoesNotContainB64Claim(jws)) {
            throw new IllegalStateException("Wrong format of Barclays payment claims: adjusted headers should contain b64 headers with value false");
        }
        super.adjustJWSHook(jws, authMeans);
    }

    private boolean jwsDoesNotContainB64Claim(final JsonWebSignature jws) {
        return !Boolean.FALSE.equals(jws.getHeaders().getObjectHeaderValue("b64"));
    }
}
