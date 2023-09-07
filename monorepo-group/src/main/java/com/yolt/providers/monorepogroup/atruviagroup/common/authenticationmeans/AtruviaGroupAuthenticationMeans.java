package com.yolt.providers.monorepogroup.atruviagroup.common.authenticationmeans;

import lombok.Builder;
import lombok.Data;

import java.security.cert.X509Certificate;
import java.util.UUID;

@Data
@Builder
public class AtruviaGroupAuthenticationMeans {

    private final UUID clientCertificateKey;
    private final X509Certificate clientCertificate;
}
