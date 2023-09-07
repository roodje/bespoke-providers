package com.yolt.providers.monorepogroup.handelsbankengroup.common.auth;

import lombok.Builder;
import lombok.Value;

import java.security.cert.X509Certificate;

@Value
@Builder
public class HandelsbankenGroupAuthMeans {

    X509Certificate transportCertificate;
    String transportKeyId;
    String tppId;
    String clientId;
    String appName;
    String appDescription;
}
