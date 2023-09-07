package com.yolt.providers.alpha.common.auth;

import com.yolt.providers.alpha.common.auth.dto.AlphaAuthMeans;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.exception.InvalidAuthenticationMeansException;
import com.yolt.providers.common.util.KeyUtil;
import org.jose4j.keys.X509Util;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.yolt.providers.common.constants.OAuth.CLIENT_ID;
import static com.yolt.providers.common.constants.OAuth.CLIENT_SECRET;
import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.CLIENT_ID_STRING;
import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.CLIENT_SECRET_STRING;

public class AlphaTypedAuthenticationMeansProducer implements TypedAuthenticationMeansProducer {

    public static final String SIGNING_KEY_HEADER_ID_NAME = "private-signing-key-header-id";
    public static final String SIGNING_CERTIFICATE_NAME = "client-signing-certificate";
    public static final String SIGNING_PRIVATE_KEY_ID_NAME = "signing-private-key-id";
    public static final String SUBSCRIPTION_KEY_NAME = "subscription-key";


    @Override
    public Map<String, TypedAuthenticationMeans> getTypedAuthenticationMeans() {
        Map<String, TypedAuthenticationMeans> typedAuthenticationMeans = new HashMap<>();
        typedAuthenticationMeans.put(SIGNING_PRIVATE_KEY_ID_NAME, TypedAuthenticationMeans.SIGNING_KEY_ID_STRING);
        typedAuthenticationMeans.put(SIGNING_CERTIFICATE_NAME, TypedAuthenticationMeans.CLIENT_SIGNING_CERTIFICATE_PEM);
        typedAuthenticationMeans.put(CLIENT_ID, CLIENT_ID_STRING);
        typedAuthenticationMeans.put(CLIENT_SECRET, CLIENT_SECRET_STRING);
        typedAuthenticationMeans.put(SUBSCRIPTION_KEY_NAME, TypedAuthenticationMeans.ALIAS_STRING);
        return typedAuthenticationMeans;
    }

    @Override
    public AlphaAuthMeans createAuthenticationMeans(final Map<String, BasicAuthenticationMean> typedAuthenticationMeans,
                                                    final String providerIdentifier) {
        return AlphaAuthMeans.builder()
                .clientId(typedAuthenticationMeans.get(CLIENT_ID).getValue())
                .clientSecret(typedAuthenticationMeans.get(CLIENT_SECRET).getValue())
                .signingKeyIdHeader(X509Util.x5tS256(createCertificate(providerIdentifier, SIGNING_CERTIFICATE_NAME, typedAuthenticationMeans)))
                .signingPrivateKeyId(UUID.fromString(typedAuthenticationMeans.get(SIGNING_PRIVATE_KEY_ID_NAME).getValue()))
                .signingCertificate(createCertificate(providerIdentifier, SIGNING_CERTIFICATE_NAME, typedAuthenticationMeans))
                .subscriptionKey(typedAuthenticationMeans.get(SUBSCRIPTION_KEY_NAME).getValue())
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
