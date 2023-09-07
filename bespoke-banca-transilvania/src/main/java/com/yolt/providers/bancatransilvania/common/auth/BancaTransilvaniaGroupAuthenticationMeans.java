package com.yolt.providers.bancatransilvania.common.auth;

import lombok.Builder;
import lombok.Data;

import java.security.cert.X509Certificate;
import java.util.UUID;

@Data
@Builder
public class BancaTransilvaniaGroupAuthenticationMeans {

    private final String clientName;
    private final String clientCompanyName;
    private final String clientWebsiteUrl;
    private final String clientContactPerson;
    private final String clientEmail;
    private final String clientPhoneNumber;
    private final X509Certificate transportCertificate;
    private final UUID transportKeyId;
    private final String clientId;
    private final String clientSecret;
}
