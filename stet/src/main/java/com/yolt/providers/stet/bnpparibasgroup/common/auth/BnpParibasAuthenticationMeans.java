package com.yolt.providers.stet.bnpparibasgroup.common.auth;

import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import lombok.Builder;
import lombok.Getter;

import java.security.cert.X509Certificate;
import java.util.UUID;

@Getter
public class BnpParibasAuthenticationMeans extends DefaultAuthenticationMeans {

    private final String registrationAccessToken;

    @Builder(builderMethodName = "extendedBuilder")
    public BnpParibasAuthenticationMeans(String clientId, String clientSecret, X509Certificate clientTransportCertificate, UUID clientTransportKeyId, X509Certificate clientSigningCertificate, UUID clientSigningKeyId, String signingKeyIdHeader, String clientName, String clientEmail, String clientWebsiteUri, String clientLogoUri, String registrationAccessToken) {
        super(clientId, clientSecret, clientTransportCertificate, clientTransportKeyId, clientSigningCertificate, clientSigningKeyId, signingKeyIdHeader, clientName, clientEmail, clientWebsiteUri, clientLogoUri);
        this.registrationAccessToken = registrationAccessToken;
    }
}
