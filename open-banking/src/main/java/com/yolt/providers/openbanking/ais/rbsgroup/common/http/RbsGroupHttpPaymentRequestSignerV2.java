package com.yolt.providers.openbanking.ais.rbsgroup.common.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.signer.ExternalPaymentRequestSigner;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.lang.JoseException;

public class RbsGroupHttpPaymentRequestSignerV2 extends ExternalPaymentRequestSigner {

    private static final String B64_HEADER_WITH_VALUE = "\"b64\":false,";

    public RbsGroupHttpPaymentRequestSignerV2(ObjectMapper objectMapper, String jwsAlgorithm) {
        super(objectMapper, jwsAlgorithm);
    }

    @Override
    protected void adjustJWSHook(JsonWebSignature jws, DefaultAuthMeans authMeans) {
        jws.setCriticalHeaderNames(CRITICAL_HEADER_OPENBANKING_IAT, CRITICAL_HEADER_OPENBANKING_ISS, CRITICAL_HEADER_OPENBANKING_TAN);
        if (jwsDoesContainB64Claim(jws)) {
            String headersAsString = jws.getHeaders().getFullHeaderAsJsonString();
            String headersWithRemovedB64HeaderAndNullKeyId = headersAsString.replace(B64_HEADER_WITH_VALUE, "");
            try {
                jws.getHeaders().setFullHeaderAsJsonString(headersWithRemovedB64HeaderAndNullKeyId);
            } catch (JoseException e) {
                throw new IllegalStateException("Error during removing b64 header");
            }
        }
    }

    private boolean jwsDoesContainB64Claim(final JsonWebSignature jws) {
        return Boolean.FALSE.equals(jws.getHeaders().getObjectHeaderValue("b64"));
    }
}
