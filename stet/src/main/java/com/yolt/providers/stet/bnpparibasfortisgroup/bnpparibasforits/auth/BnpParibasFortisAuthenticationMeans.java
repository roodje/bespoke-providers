package com.yolt.providers.stet.bnpparibasfortisgroup.bnpparibasforits.auth;

import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import lombok.Builder;
import lombok.Getter;

import java.security.cert.X509Certificate;
import java.util.UUID;

@Getter
public class BnpParibasFortisAuthenticationMeans extends DefaultAuthenticationMeans {

    private final String clientDescription;
    private final String contactFirstName;
    private final String contactLastName;
    private final String contactPhone;

    @Builder(builderMethodName = "extendedBuilder")
    public BnpParibasFortisAuthenticationMeans(String clientId, //NOSONAR It is used to override builder
                                               String clientSecret,
                                               X509Certificate clientTransportCertificate,
                                               UUID clientTransportKeyId,
                                               X509Certificate clientSigningCertificate,
                                               UUID clientSigningKeyId,
                                               String signingKeyIdHeader,
                                               String clientName,
                                               String clientEmail,
                                               String clientWebsiteUri,
                                               String clientDescription,
                                               String contactFirstName,
                                               String contactLastName,
                                               String contactPhone) {
        super(clientId, clientSecret, clientTransportCertificate, clientTransportKeyId, clientSigningCertificate, clientSigningKeyId, signingKeyIdHeader, clientName, clientEmail, clientWebsiteUri);
        this.clientDescription = clientDescription;
        this.contactFirstName = contactFirstName;
        this.contactLastName = contactLastName;
        this.contactPhone = contactPhone;
    }
}
