package com.yolt.providers.argentagroup.common.http;

import com.yolt.providers.argentagroup.common.exception.CertificateFormattingException;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;

import java.io.IOException;
import java.io.StringWriter;
import java.security.cert.X509Certificate;

public class TppSignatureCertificateHeaderProducer {

    public String getTppSignatureCertificateHeaderValue(X509Certificate signingCertificate) {
        StringWriter sw = new StringWriter();
        try (JcaPEMWriter jpw = new JcaPEMWriter(sw)) {
            jpw.writeObject(signingCertificate);
        } catch (IOException e) {
            throw new CertificateFormattingException("Could not convert certificate to PEM format");
        }
        String pem = sw.toString();

        return pem.replaceAll(System.lineSeparator(), "    ").strip();
    }
}
