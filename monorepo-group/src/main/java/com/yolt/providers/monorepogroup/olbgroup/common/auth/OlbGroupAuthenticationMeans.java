package com.yolt.providers.monorepogroup.olbgroup.common.auth;

import lombok.Builder;
import lombok.Value;

import java.security.cert.X509Certificate;

@Value
@Builder
public class OlbGroupAuthenticationMeans {

    private final X509Certificate transportCertificate;
    private final String transportKeyId;
}
