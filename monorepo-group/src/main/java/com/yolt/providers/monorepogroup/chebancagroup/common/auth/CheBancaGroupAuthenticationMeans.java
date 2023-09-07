package com.yolt.providers.monorepogroup.chebancagroup.common.auth;

import lombok.Value;

import java.security.cert.X509Certificate;
import java.util.UUID;

@Value
public class CheBancaGroupAuthenticationMeans {

    public static final String TRANSPORT_CERTIFICATE_NAME = "transport-certificate";
    public static final String TRANSPORT_CERTIFICATE_ID_NAME = "transport-certificate-id";
    public static final String SIGNING_CERTIFICATE_NAME = "signing-certificate";
    public static final String SIGNING_CERTIFICATE_ID_NAME = "signing-certificate-id";
    public static final String CLIENT_ID_NAME = "client-id";
    public static final String CLIENT_SECRET_NAME = "client-secret";
    public static final String CLIENT_APP_ID = "client-app-id";

    private UUID transportCertificateId;
    private X509Certificate transportCertificate;
    private UUID signingCertificateId;
    private X509Certificate signingCertificate;
    private String clientId;
    private String clientSecret;
    private String clientAppId;
}
