package com.yolt.providers.openbanking.ais.generic2.signer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.lang.JoseException;

public class ExternalPaymentNoKidRequestSignerDecorator extends ExternalPaymentRequestSigner {

    private static final String KEY_ID_HEADER_WITH_VALUE = ",\"kid\":null";

    private final ExternalPaymentRequestSigner wrappee;

    public ExternalPaymentNoKidRequestSignerDecorator(ObjectMapper objectMapper, String jwsAlgorithm, ExternalPaymentRequestSigner wrappee) {
        super(objectMapper, jwsAlgorithm);
        this.wrappee = wrappee;
    }

    @Override
    protected void adjustJWSHook(JsonWebSignature jws, DefaultAuthMeans authMeans) {
        wrappee.adjustJWSHook(jws, authMeans);

        try {
            jws.getHeaders().setFullHeaderAsJsonString(jws.getHeaders().getFullHeaderAsJsonString().replace(KEY_ID_HEADER_WITH_VALUE, ""));
        } catch (JoseException e) {
            throw new IllegalStateException("Error during removing kid header");
        }
    }

}
