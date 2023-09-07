package com.yolt.providers.n26.common.auth;

import lombok.Builder;
import lombok.Data;

import java.security.cert.X509Certificate;
import java.util.UUID;

@Data
@Builder
public class N26GroupAuthenticationMeans {

    private final String clientId;
    private final UUID transportKeyId;
    private final X509Certificate transportCertificate;
}
