package com.yolt.providers.amexgroup.common.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.security.cert.X509Certificate;
import java.util.UUID;

@Data
@AllArgsConstructor
public class AmexGroupAuthMeansV6 implements AmexGroupAuthMeans {

    private final String clientId;
    private final String clientSecret;
    private final X509Certificate clientTransportCertificate;
    private final UUID transportKeyId;
}