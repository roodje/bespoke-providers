package com.yolt.providers.openbanking.ais.monzogroup.common.http.paymenthttppayloadsigner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.signer.ExternalPaymentNoB64RequestSigner;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwx.HeaderParameterNames;

public class MonzoGroupExternalPaymentRequestSignerV2 extends ExternalPaymentNoB64RequestSigner {

    public MonzoGroupExternalPaymentRequestSignerV2(ObjectMapper objectMapper, String jwsAlgorithm) {
        super(objectMapper, jwsAlgorithm);
    }

    /**
     * Bank does not support critical header b64 and returns 400 bad request if one is provided,
     * hence it is overridden to remove it from critical headers list and set this header value to null.
     *
     * @param jws
     */
    @Override
    protected void adjustJWSHook(JsonWebSignature jws, DefaultAuthMeans authMeans) {
        super.adjustJWSHook(jws, authMeans);
        jws.getHeaders().setObjectHeaderValue(HeaderParameterNames.BASE64URL_ENCODE_PAYLOAD, null);
    }
}