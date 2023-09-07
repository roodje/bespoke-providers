package com.yolt.providers.amexgroup.common.auth;

import java.security.cert.X509Certificate;
import java.util.UUID;

public interface AmexGroupAuthMeans {

    String getClientId();

    String getClientSecret();

    X509Certificate getClientTransportCertificate();

    UUID getTransportKeyId();
}