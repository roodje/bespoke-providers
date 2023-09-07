package com.yolt.providers.stet.boursoramagroup.boursorama.auth;

import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import lombok.Builder;
import lombok.Getter;

import java.security.cert.X509Certificate;
import java.util.UUID;

@Getter
public class BoursoramaAuthenticationMeans extends DefaultAuthenticationMeans {

    private final String certificateAgreementNumber;

    @Builder(builderMethodName = "extendedBuilder")
    public BoursoramaAuthenticationMeans(String clientId, //NOSONAR It is used to override builder
                                         String clientSecret,
                                         X509Certificate clientTransportCertificate,
                                         UUID clientTransportKeyId,
                                         X509Certificate clientSigningCertificate,
                                         UUID clientSigningKeyId,
                                         String signingKeyIdHeader,
                                         String clientName,
                                         String clientEmail,
                                         String clientWebsiteUri,
                                         String certificateAgreementNumber) {
        super(clientId, clientSecret, clientTransportCertificate, clientTransportKeyId, clientSigningCertificate, clientSigningKeyId, signingKeyIdHeader, clientName, clientEmail, clientWebsiteUri);
        this.certificateAgreementNumber = certificateAgreementNumber;
    }
}
