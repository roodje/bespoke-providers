package com.yolt.providers.deutschebank.common.auth;

import lombok.Builder;
import lombok.Data;

import java.security.cert.X509Certificate;

@Data
@Builder
public class DeutscheBankGroupAuthenticationMeans {

    private final X509Certificate transportCertificate;
    private final String transportKeyId;
}
