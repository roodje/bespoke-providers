package com.yolt.providers.alpha.common.auth.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.security.cert.X509Certificate;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class AlphaAuthMeans {
    private final String clientId;
    private final String clientSecret;
    private final String signingKeyIdHeader;
    private final UUID signingPrivateKeyId;
    private final X509Certificate signingCertificate;
    private final String subscriptionKey;
}