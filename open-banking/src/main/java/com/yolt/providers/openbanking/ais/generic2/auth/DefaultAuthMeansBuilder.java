package com.yolt.providers.openbanking.ais.generic2.auth;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.InvalidAuthenticationMeansException;
import com.yolt.providers.common.util.KeyUtil;
import org.springframework.util.StringUtils;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.UUID;

public class DefaultAuthMeansBuilder {

    public static final String INSTITUTION_ID_NAME = "institution-id";
    public static final String CLIENT_ID_NAME = "client-id";
    public static final String CLIENT_SECRET_NAME = "client-secret";
    public static final String SIGNING_KEY_HEADER_ID_NAME = "private-signing-key-header-id";
    public static final String TRANSPORT_CERTIFICATE_NAME = "transport-certificate";
    public static final String TRANSPORT_PRIVATE_KEY_ID_NAME = "transport-private-key-id";
    public static final String SIGNING_PRIVATE_KEY_ID_NAME = "signing-private-key-id";

    public DefaultAuthMeans createAuthenticationMeans(final Map<String, BasicAuthenticationMean> typedAuthenticationMeans,
                                                      final String provider) {
        return createDefaultAuthenticationMeansBuilder(typedAuthenticationMeans, provider)
                .build();
    }

    public DefaultAuthMeans.DefaultAuthMeansBuilder createDefaultAuthenticationMeansBuilder(final Map<String, BasicAuthenticationMean> typedAuthenticationMeans,
                                                                                            final String provider) {
        return DefaultAuthMeans.builder()
                .institutionId(typedAuthenticationMeans.get(INSTITUTION_ID_NAME).getValue())
                .clientId(typedAuthenticationMeans.get(CLIENT_ID_NAME).getValue())
                .clientSecret(typedAuthenticationMeans.get(CLIENT_SECRET_NAME).getValue())
                .signingKeyIdHeader(typedAuthenticationMeans.get(SIGNING_KEY_HEADER_ID_NAME).getValue())
                .signingPrivateKeyId(UUID.fromString(typedAuthenticationMeans.get(SIGNING_PRIVATE_KEY_ID_NAME).getValue()))
                .transportCertificate(createCertificate(typedAuthenticationMeans.get(TRANSPORT_CERTIFICATE_NAME).getValue(), provider))
                .transportPrivateKeyId(UUID.fromString(typedAuthenticationMeans.get(TRANSPORT_PRIVATE_KEY_ID_NAME).getValue()));
    }

    private X509Certificate createCertificate(final String certificate, final String provider) {
        try {
            if (!StringUtils.isEmpty(certificate))
                return KeyUtil.createCertificateFromPemFormat(certificate);
            else {
                return null;
            }
        } catch (CertificateException e) {
            throw new InvalidAuthenticationMeansException(provider, TRANSPORT_CERTIFICATE_NAME, "Cannot process certificate for thumbprint");
        }
    }
}
