package com.yolt.providers.commerzbankgroup.common.authmeans;


import lombok.Builder;
import lombok.Data;

import java.security.cert.X509Certificate;
import java.util.UUID;

@Builder
@Data
public class DefaultCommerzbankgroupAuthenticationMeans implements CommerzbankGroupAuthenticationMeans {

    private final UUID clientCertificateKey;
    private final X509Certificate clientCertificate;
    private final String organizationIdentifier;
}
