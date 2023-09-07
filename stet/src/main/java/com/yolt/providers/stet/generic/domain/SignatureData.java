package com.yolt.providers.stet.generic.domain;

import com.yolt.providers.common.cryptography.Signer;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpMethod;

import java.security.cert.X509Certificate;
import java.util.UUID;

@Data
@AllArgsConstructor
public class SignatureData {

    private final Signer signer;
    private final String headerKeyId;
    private final UUID signingKeyId;
    private final X509Certificate signingCertificate;
    private final HttpMethod httpMethod;
    private final String host;
    private final String endpoint;
    private final String clientId;

    public SignatureData(Signer signer,
                         String headerKeyId,
                         UUID signingKeyId,
                         X509Certificate signingCertificate,
                         HttpMethod httpMethod,
                         String endpoint) {
        this.signer = signer;
        this.headerKeyId = headerKeyId;
        this.signingKeyId = signingKeyId;
        this.signingCertificate = signingCertificate;
        this.httpMethod = httpMethod;
        this.host = "";
        this.endpoint = endpoint;
        this.clientId = null;
    }

    public SignatureData(Signer signer,
                         String headerKeyId,
                         UUID signingKeyId,
                         X509Certificate signingCertificate,
                         HttpMethod httpMethod,
                         String host,
                         String endpoint) {
        this.signer = signer;
        this.headerKeyId = headerKeyId;
        this.signingKeyId = signingKeyId;
        this.signingCertificate = signingCertificate;
        this.httpMethod = httpMethod;
        this.host = host;
        this.endpoint = endpoint;
        this.clientId = null;
    }
}
