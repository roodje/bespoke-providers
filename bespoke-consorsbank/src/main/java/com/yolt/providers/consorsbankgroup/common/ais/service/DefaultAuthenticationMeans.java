package com.yolt.providers.consorsbankgroup.common.ais.service;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.InvalidAuthenticationMeansException;
import com.yolt.providers.common.util.KeyUtil;
import lombok.Data;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Data
public class DefaultAuthenticationMeans {

    public static final String CLIENT_TRANSPORT_CERTIFICATE_NAME = "client-transport-certificate";
    public static final String CLIENT_TRANSPORT_KEY_ID_NAME = "client-transport-key-id";

    private final UUID clientTransportKeyId;
    private final X509Certificate clientTransportCertificate;

    private DefaultAuthenticationMeans(final String providerIdentifier,
                                       final String clientTransportKeyId,
                                       final String clientTransportCertificateString) {
        Objects.requireNonNull(providerIdentifier, "providerIdentifier cannot be null");
        Objects.requireNonNull(clientTransportKeyId, "clientTransportKeyId cannot be null");
        Objects.requireNonNull(clientTransportCertificateString, "clientTransportCertificateString cannot be null");
        this.clientTransportKeyId = UUID.fromString(clientTransportKeyId);
        this.clientTransportCertificate = createCertificate(clientTransportCertificateString, providerIdentifier);
    }

    public static DefaultAuthenticationMeans fromAuthMeans(final String providerIdentifier,
                                                           final Map<String, BasicAuthenticationMean> authMeans) {
        return new DefaultAuthenticationMeans(
                providerIdentifier,
                authMeans.get(CLIENT_TRANSPORT_KEY_ID_NAME).getValue(),
                authMeans.get(CLIENT_TRANSPORT_CERTIFICATE_NAME).getValue()
        );
    }

    private static X509Certificate createCertificate(final String certificateString,
                                                     final String provider) {
        try {
            return KeyUtil.createCertificateFromPemFormat(certificateString);
        } catch (CertificateException e) {
            throw new InvalidAuthenticationMeansException(provider, CLIENT_TRANSPORT_CERTIFICATE_NAME, "Cannot process certificate for thumbprint");
        }
    }
}
