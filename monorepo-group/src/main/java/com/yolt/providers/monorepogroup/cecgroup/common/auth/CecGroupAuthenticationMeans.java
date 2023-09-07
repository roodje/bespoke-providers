package com.yolt.providers.monorepogroup.cecgroup.common.auth;

import lombok.Builder;
import lombok.Value;

import java.security.cert.X509Certificate;

@Value
@Builder
public class CecGroupAuthenticationMeans {

    X509Certificate transportCertificate;
    String transportKeyId;
    X509Certificate signingCertificate;
    String signingKeyId;
    String clientId;
    String clientSecret;
}
