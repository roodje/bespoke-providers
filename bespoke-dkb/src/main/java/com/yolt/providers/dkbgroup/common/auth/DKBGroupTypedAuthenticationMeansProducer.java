package com.yolt.providers.dkbgroup.common.auth;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.exception.InvalidAuthenticationMeansException;
import com.yolt.providers.common.util.KeyUtil;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DKBGroupTypedAuthenticationMeansProducer implements TypedAuthenticationMeansProducer {

    public static final String TRANSPORT_CERTIFICATE_NAME = "client-transport-certificate";
    public static final String TRANSPORT_KEY_ID_NAME = "private-client-transport-key-id";

    @Override
    public Map<String, TypedAuthenticationMeans> getTypedAuthenticationMeans() {
        Map<String, TypedAuthenticationMeans> typedAuthenticationMeans = new HashMap<>();
        typedAuthenticationMeans.put(TRANSPORT_CERTIFICATE_NAME, TypedAuthenticationMeans.CLIENT_TRANSPORT_CERTIFICATE_PEM);
        typedAuthenticationMeans.put(TRANSPORT_KEY_ID_NAME, TypedAuthenticationMeans.KEY_ID);
        return typedAuthenticationMeans;
    }

    @Override
    public DKBGroupAuthMeans createAuthenticationMeans(final Map<String, BasicAuthenticationMean> typedAuthenticationMeans,
                                                       final String providerIdentifier) {
        return DKBGroupAuthMeans.builder()
                .transportKeyId(UUID.fromString(typedAuthenticationMeans.get(TRANSPORT_KEY_ID_NAME).getValue()))
                .transportCertificate(createCertificate(providerIdentifier, TRANSPORT_CERTIFICATE_NAME, typedAuthenticationMeans))
                .build();
    }

    private X509Certificate createCertificate(final String providerIdentifier,
                                              final String certificateName,
                                              final Map<String, BasicAuthenticationMean> typedAuthenticationMeans) {
        try {
            return KeyUtil.createCertificateFromPemFormat(typedAuthenticationMeans.get(certificateName).getValue());
        } catch (CertificateException e) {
            throw new InvalidAuthenticationMeansException(providerIdentifier, certificateName,
                    "Cannot process certificate from PEM format");
        }
    }
}
