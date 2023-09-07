package com.yolt.providers.unicredit.common.auth;

import lombok.Builder;
import lombok.Value;

import java.security.cert.X509Certificate;

@Value
@Builder
public class UniCreditAuthMeans {

    public static final String EIDAS_CERTIFICATE = "eidas-certificate";
    public static final String EIDAS_KEY_ID = "eidas-key-id";
    public static final String CLIENT_EMAIL = "client-email";
    public static final String REGISTRATION_STATUS = "reg-status";

    private final X509Certificate eidasCertificate;
    private final String eidasKeyId;
    private final String clientEmail;
}
