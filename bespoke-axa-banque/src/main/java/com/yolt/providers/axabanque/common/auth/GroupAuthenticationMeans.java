package com.yolt.providers.axabanque.common.auth;

import lombok.Value;

import java.security.cert.X509Certificate;
import java.util.UUID;

@Value
public class GroupAuthenticationMeans {
    public static final String TRANSPORT_CERTIFICATE = "transport-certificate";
    public static final String TRANSPORT_KEY_ID = "transport-key-id";
    public static final String CLIENT_ID = "client-id";

    UUID transportKeyId;
    X509Certificate tlsCertificate;
    String clientId;
}
