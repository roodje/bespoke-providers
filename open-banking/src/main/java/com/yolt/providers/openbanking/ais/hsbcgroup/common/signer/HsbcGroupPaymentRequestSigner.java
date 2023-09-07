package com.yolt.providers.openbanking.ais.hsbcgroup.common.signer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.signer.ExternalPaymentNoB64RequestSigner;
import org.jose4j.jws.JsonWebSignature;

import java.security.cert.X509Certificate;
import java.util.function.Function;

public class HsbcGroupPaymentRequestSigner extends ExternalPaymentNoB64RequestSigner {
    private final Function<X509Certificate, String> certificateIdentityExtractor;

    public HsbcGroupPaymentRequestSigner(ObjectMapper objectMapper, String jwsAlgorithm,
                                         Function<X509Certificate, String> certificateIdentityExtractor) {
        super(objectMapper, jwsAlgorithm);
        this.certificateIdentityExtractor = certificateIdentityExtractor;
    }

    @Override
    protected void adjustJWSHook(JsonWebSignature jws, DefaultAuthMeans authMeans) {
        X509Certificate transportCertificate = authMeans.getTransportCertificate();
        jws.setHeader(CRITICAL_HEADER_OPENBANKING_ISS, certificateIdentityExtractor.apply(transportCertificate));
        super.adjustJWSHook(jws, authMeans);
    }
}
