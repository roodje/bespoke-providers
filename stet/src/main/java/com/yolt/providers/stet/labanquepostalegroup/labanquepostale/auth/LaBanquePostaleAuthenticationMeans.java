package com.yolt.providers.stet.labanquepostalegroup.labanquepostale.auth;

import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import lombok.Builder;
import lombok.Getter;

import java.security.cert.X509Certificate;
import java.util.UUID;

@Getter
public class LaBanquePostaleAuthenticationMeans extends DefaultAuthenticationMeans {

    private final String portalUsername;
    private final String portalPassword;
    private final X509Certificate[] clientTransportCertificateChain;

    @Builder(builderMethodName = "extendedBuilder")
    public LaBanquePostaleAuthenticationMeans(String clientId, //NOSONAR It is used to override builder
                                              String clientSecret,
                                              X509Certificate[] clientTransportCertificateChain,
                                              UUID clientTransportKeyId,
                                              X509Certificate clientSigningCertificate,
                                              UUID clientSigningKeyId,
                                              String signingKeyIdHeader,
                                              String clientName,
                                              String clientEmail,
                                              String clientWebsiteUri,
                                              String portalUsername,
                                              String portalPassword) {
        super(clientId, clientSecret,  null, clientTransportKeyId, clientSigningCertificate, clientSigningKeyId, signingKeyIdHeader, clientName, clientEmail, clientWebsiteUri);
        this.portalUsername = portalUsername;
        this.portalPassword = portalPassword;
        this.clientTransportCertificateChain = clientTransportCertificateChain;
    }
}
