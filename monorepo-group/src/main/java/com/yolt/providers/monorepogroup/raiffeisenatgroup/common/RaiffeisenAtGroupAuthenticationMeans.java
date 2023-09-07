package com.yolt.providers.monorepogroup.raiffeisenatgroup.common;

import lombok.Value;

import java.security.cert.X509Certificate;
import java.util.UUID;

@Value
public class RaiffeisenAtGroupAuthenticationMeans {

    public static final String TRANSPORT_CERTIFICATE_NAME = "transport-certificate";
    public static final String TRANSPORT_CERTIFICATE_ID_NAME = "transport-certificate-id";
    public static final String CLIENT_ID_NAME = "client-id";

    private UUID transportCertificateId;
    private X509Certificate transportCertificate;
    private String clientId;

}
