package com.yolt.providers.redsys.common.model;

import com.yolt.providers.common.cryptography.Signer;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.UUID;

@Getter
@Builder
public class SignatureData {
    @NonNull
    private Signer signer;
    @NonNull
    private UUID signingKeyId;
    @NonNull
    private X509Certificate signingCertificate;

    @SneakyThrows(CertificateEncodingException.class)
    public byte[] getEncodedSigningCertificate() {
        return signingCertificate.getEncoded();
    }
}