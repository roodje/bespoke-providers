package com.yolt.providers.openbanking.ais.generic2.auth;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.security.cert.X509Certificate;
import java.util.UUID;

@Slf4j
@Data
@Builder
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class DefaultAuthMeans {

    private final String institutionId;
    private final String clientId;
    private final String clientSecret;
    private final String signingKeyIdHeader;
    private final X509Certificate signingCertificate;
    private final X509Certificate transportCertificate;
    private final X509Certificate[] transportCertificatesChain;
    private final UUID transportPrivateKeyId;
    private final UUID signingPrivateKeyId;
    private final String certificateId;
    private final String organizationId;
    private final String softwareId;
    private final String registrationAccessToken;
}
