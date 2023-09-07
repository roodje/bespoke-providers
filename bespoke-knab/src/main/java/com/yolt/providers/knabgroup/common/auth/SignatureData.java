package com.yolt.providers.knabgroup.common.auth;

import com.yolt.providers.common.cryptography.Signer;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

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
    @NonNull
    private final String signingCertificateInBase64;
}