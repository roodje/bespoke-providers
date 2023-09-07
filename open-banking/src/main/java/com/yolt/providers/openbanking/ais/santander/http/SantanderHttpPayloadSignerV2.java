package com.yolt.providers.openbanking.ais.santander.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.signer.ExternalPaymentNoB64RequestSigner;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.lang.JoseException;

public class SantanderHttpPayloadSignerV2 extends ExternalPaymentNoB64RequestSigner {

    private static final String KEY_ID_HEADER_WITH_VALUE = ",\"kid\":null";


    public SantanderHttpPayloadSignerV2(ObjectMapper objectMapper, String jwsAlgorithm) {
        super(objectMapper, jwsAlgorithm);
    }

    @Override
    protected void adjustJWSHook(JsonWebSignature jws, DefaultAuthMeans authMeans) {
        if (!Boolean.FALSE.equals(jws.getHeaders().getObjectHeaderValue("b64"))) {
            throw new IllegalStateException("Wrong format of Santander payment claims: adjusted headers should contain b64 headers with value false");
        }

        super.adjustJWSHook(jws, authMeans);

        try {
            jws.getHeaders().setFullHeaderAsJsonString(jws.getHeaders().getFullHeaderAsJsonString().replace(KEY_ID_HEADER_WITH_VALUE, ""));
        } catch (JoseException e) {
            throw new IllegalStateException("Error during removing kid header");
        }
    }
}