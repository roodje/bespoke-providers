package com.yolt.providers.argentagroup.common.http;


import com.yolt.providers.common.cryptography.Signer;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.springframework.http.HttpHeaders;

import java.security.cert.X509Certificate;
import java.util.List;
import java.util.UUID;

@Getter
@NonNull
@AllArgsConstructor
public class SignatureData {

    private final UUID signingKeyId;
    private final Signer signer;
    private final X509Certificate signingCertificate;
    private final HttpHeaders headers;
    private final List<String> headersToSign;
    private final SignatureAlgorithm signatureAlgorithm;
}
