package com.yolt.providers.stet.generic.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.security.cert.X509Certificate;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class DefaultAuthenticationMeans {

    private final String clientId;
    private final String clientSecret;
    private final X509Certificate clientTransportCertificate;
    private final UUID clientTransportKeyId;
    private final X509Certificate clientSigningCertificate;
    private final UUID clientSigningKeyId;
    private final String signingKeyIdHeader;
    private final String clientName;
    private final String clientEmail;
    private final String clientWebsiteUri;
    private final String clientLogoUri;

    public DefaultAuthenticationMeans(String clientId, String clientSecret, X509Certificate clientTransportCertificate, UUID clientTransportKeyId, X509Certificate clientSigningCertificate, UUID clientSigningKeyId, String signingKeyIdHeader, String clientName, String clientEmail, String clientWebsiteUri) {
 this(clientId, clientSecret, clientTransportCertificate, clientTransportKeyId, clientSigningCertificate, clientSigningKeyId, signingKeyIdHeader, clientName, clientEmail, clientWebsiteUri, null);
    }
}
