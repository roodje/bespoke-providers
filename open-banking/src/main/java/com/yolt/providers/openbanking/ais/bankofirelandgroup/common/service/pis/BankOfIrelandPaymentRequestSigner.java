package com.yolt.providers.openbanking.ais.bankofirelandgroup.common.service.pis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.signer.ExternalPaymentNoTanRequestSigner;
import org.jose4j.jws.JsonWebSignature;

public class BankOfIrelandPaymentRequestSigner extends ExternalPaymentNoTanRequestSigner {

    public BankOfIrelandPaymentRequestSigner(ObjectMapper objectMapper, String jwsAlgorithm) {
        super(objectMapper, jwsAlgorithm);
    }

    @Override
    protected void adjustJWSHook(JsonWebSignature jws, DefaultAuthMeans authMeans) {
        jws.setHeader(CRITICAL_HEADER_OPENBANKING_ISS, authMeans.getTransportCertificate().getSubjectDN().toString());
        super.adjustJWSHook(jws, authMeans);
    }
}
