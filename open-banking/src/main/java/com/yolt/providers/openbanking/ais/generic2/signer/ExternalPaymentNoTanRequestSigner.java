package com.yolt.providers.openbanking.ais.generic2.signer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwx.HeaderParameterNames;
import org.jose4j.lang.JoseException;

public class ExternalPaymentNoTanRequestSigner extends ExternalPaymentRequestSigner {

    private static final String TAN_HEADER_WITH_VALUE = "\"http://openbanking.org.uk/tan\":\"openbanking.org.uk\",";

    public ExternalPaymentNoTanRequestSigner(ObjectMapper objectMapper, String jwsAlgorithm) {
        super(objectMapper, jwsAlgorithm);
    }

    @Override
    protected void adjustJWSHook(JsonWebSignature jws, DefaultAuthMeans authMeans) {
        jws.setCriticalHeaderNames(HeaderParameterNames.BASE64URL_ENCODE_PAYLOAD, CRITICAL_HEADER_OPENBANKING_IAT,
                CRITICAL_HEADER_OPENBANKING_ISS);
        String headersAsString = jws.getHeaders().getFullHeaderAsJsonString();
        String headersWithoutTan = headersAsString.replace(TAN_HEADER_WITH_VALUE, "");
        try {
            jws.getHeaders().setFullHeaderAsJsonString(headersWithoutTan); //Take care this assigns headers to disallow duplicates!
        } catch (JoseException e) {
            throw new IllegalStateException("Error during removing tan header");
        }
    }
}
