package com.yolt.providers.openbanking.ais.permanenttsbgroup.common.oauth2;

import lombok.NoArgsConstructor;
import org.springframework.util.Base64Utils;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

@NoArgsConstructor
public class PermanentTsbGroupTppSignatureCertificateHeaderProducer {

    private static final String BEGIN_TAG = "-----BEGIN CERTIFICATE-----";
    private static final String END_TAG = "-----END CERTIFICATE-----";

    public String getTppSignatureCertificateHeaderValue(X509Certificate signingCertificate) throws CertificateEncodingException {
        String certificatePem = Base64Utils.encodeToString(signingCertificate.getEncoded());
        String taggedCertificatePem = BEGIN_TAG + certificatePem + END_TAG;
        return Base64Utils.encodeToString(taggedCertificatePem.getBytes());
    }
}
