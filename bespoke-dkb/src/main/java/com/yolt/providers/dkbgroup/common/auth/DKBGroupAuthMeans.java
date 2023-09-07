package com.yolt.providers.dkbgroup.common.auth;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.security.cert.X509Certificate;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class DKBGroupAuthMeans {
    private final UUID transportKeyId;
    private final X509Certificate transportCertificate;
}