package com.yolt.providers.triodosbank.common.model.domain;

import com.yolt.providers.common.cryptography.Signer;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Base64;
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
    public String getBase64EncodedSigningCertificate() {
        return Base64.getEncoder().encodeToString(signingCertificate.getEncoded());
    }
}
