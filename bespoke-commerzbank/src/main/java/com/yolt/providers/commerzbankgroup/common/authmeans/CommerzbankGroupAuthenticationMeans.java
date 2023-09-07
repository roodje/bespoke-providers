package com.yolt.providers.commerzbankgroup.common.authmeans;

import java.security.cert.X509Certificate;
import java.util.UUID;

public interface CommerzbankGroupAuthenticationMeans {

    UUID getClientCertificateKey();
    X509Certificate getClientCertificate();
    String getOrganizationIdentifier();
}


