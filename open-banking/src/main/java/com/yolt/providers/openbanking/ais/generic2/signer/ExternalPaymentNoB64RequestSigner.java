package com.yolt.providers.openbanking.ais.generic2.signer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.lang.JoseException;

public class ExternalPaymentNoB64RequestSigner extends ExternalPaymentRequestSigner {

    private static final String B64_HEADER_WITH_VALUE = "\"b64\":false,";

    public ExternalPaymentNoB64RequestSigner(ObjectMapper objectMapper, String jwsAlgorithm) {
        super(objectMapper, jwsAlgorithm);
    }

    @Override
    protected void adjustJWSHook(JsonWebSignature jws, DefaultAuthMeans authMeans) {
        jws.setCriticalHeaderNames(CRITICAL_HEADER_OPENBANKING_IAT, CRITICAL_HEADER_OPENBANKING_ISS, CRITICAL_HEADER_OPENBANKING_TAN);
        String headersAsString = jws.getHeaders().getFullHeaderAsJsonString();
        String headersWithoutB64 = headersAsString.replace(B64_HEADER_WITH_VALUE, "");
        try {
            jws.getHeaders().setFullHeaderAsJsonString(headersWithoutB64); //Take care this assigns headers to disallow duplicates!
        } catch (JoseException e) {
            throw new IllegalStateException("Error during removing b64 header");
        }
    }
}
