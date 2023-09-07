package com.yolt.providers.argentagroup.common.service;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.exception.InvalidAuthenticationMeansException;
import com.yolt.providers.common.util.KeyUtil;
import lombok.Getter;
import lombok.NonNull;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.UUID;

@Getter
public class DefaultAuthenticationMeans {

    public static final String TRANSPORT_CERTIFICATE_NAME = "transport-certificate";
    public static final String TRANSPORT_KEY_ID_NAME = "transport-key-id";
    public static final String SIGNING_CERTIFICATE_NAME = "signing-certificate";
    public static final String SIGNING_KEY_ID_NAME = "signing-key-id";
    public static final String API_KEY_NAME = "api-key";
    public static final String CLIENT_ID_NAME = "client-id";

    @NonNull
    private final UUID transportKeyId;
    @NonNull
    private final X509Certificate transportCertificate;
    @NonNull
    private final UUID signingKeyId;
    @NonNull
    private final X509Certificate signingCertificate;
    @NonNull
    private final String apiKey;
    @NonNull
    private final String clientId;

    private DefaultAuthenticationMeans(final String providerIdentifier,
                                       final String transportKeyId,
                                       final String clientTransportCertificateString,
                                       final String signingKeyId,
                                       final String signingCertificateString,
                                       final String apiKey,
                                       final String clientId) {
        this.transportKeyId = UUID.fromString(transportKeyId);
        this.transportCertificate = createCertificate(clientTransportCertificateString, TRANSPORT_CERTIFICATE_NAME, providerIdentifier);
        this.signingKeyId = UUID.fromString(signingKeyId);
        this.signingCertificate = createCertificate(signingCertificateString, SIGNING_CERTIFICATE_NAME, providerIdentifier);
        this.apiKey = apiKey;
        this.clientId = clientId;
    }

    public static DefaultAuthenticationMeans fromAuthMeans(final String providerIdentifier,
                                                           final Map<String, BasicAuthenticationMean> authMeans) {
        return new DefaultAuthenticationMeans(
                providerIdentifier,
                authMeans.get(TRANSPORT_KEY_ID_NAME).getValue(),
                authMeans.get(TRANSPORT_CERTIFICATE_NAME).getValue(),
                authMeans.get(SIGNING_KEY_ID_NAME).getValue(),
                authMeans.get(SIGNING_CERTIFICATE_NAME).getValue(),
                authMeans.get(API_KEY_NAME).getValue(),
                authMeans.get(CLIENT_ID_NAME).getValue()
        );
    }

    public static Map<String, TypedAuthenticationMeans> getTypedAuthenticationMeans() {
        return Map.ofEntries(
                Map.entry(TRANSPORT_CERTIFICATE_NAME, TypedAuthenticationMeans.CLIENT_TRANSPORT_CERTIFICATE_PEM),
                Map.entry(TRANSPORT_KEY_ID_NAME, TypedAuthenticationMeans.KEY_ID),
                Map.entry(SIGNING_CERTIFICATE_NAME, TypedAuthenticationMeans.CLIENT_SIGNING_CERTIFICATE_PEM),
                Map.entry(SIGNING_KEY_ID_NAME, TypedAuthenticationMeans.KEY_ID),
                Map.entry(API_KEY_NAME, TypedAuthenticationMeans.API_KEY_STRING),
                Map.entry(CLIENT_ID_NAME, TypedAuthenticationMeans.CLIENT_ID_STRING)
        );
    }

    private static X509Certificate createCertificate(final String certificateString,
                                                     final String certificateType,
                                                     final String provider) {
        try {
            return KeyUtil.createCertificateFromPemFormat(certificateString);
        } catch (CertificateException e) {
            throw new InvalidAuthenticationMeansException(provider, certificateType, "Cannot process certificate for thumbprint");
        }
    }

}
