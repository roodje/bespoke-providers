package com.yolt.providers.openbanking.ais.hsbcgroup.common.auth;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.InvalidAuthenticationMeansException;
import com.yolt.providers.common.util.KeyUtil;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans.DefaultAuthMeansBuilder;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HsbcGroupAuthMeansBuilderV3 {

    public static final String CLIENT_ID_NAME = "client-id-2";
    public static final String INSTITUTION_ID_NAME = "institution-id-2";
    public static final String ISSUER_NAME = "issuer-2";
    public static final String SOFTWARE_STATEMENT_ASSERTION_NAME = "software-statement-assertion-2";
    public static final String SOFTWARE_ID_NAME = "software-id-2";

    public static final String PRIVATE_SIGNING_KEY_HEADER_ID_NAME = "private-signing-key-header-id-2";
    public static final String TRANSPORT_CERTIFICATE_NAME = "transport-certificate-2";
    public static final String SIGNING_PRIVATE_KEY_ID_NAME = "signing-private-key-id-2";
    public static final String TRANSPORT_PRIVATE_KEY_ID_NAME = "transport-private-key-id-2";

    public static final String ORGANIZATION_ID_NAME = "organization-id-2";

    public static DefaultAuthMeans createAuthenticationMeansForAis(final Map<String, BasicAuthenticationMean> typedAuthenticationMeans,
                                                                   final String providerKey) {
        return prepareDefaultAuthMeansBuilder(typedAuthenticationMeans, providerKey)
                .build();
    }

    public static DefaultAuthMeans createAuthenticationMeansForPis(final Map<String, BasicAuthenticationMean> typedAuthenticationMeans,
                                                                   final String providerKey) {
        return prepareDefaultAuthMeansBuilder(typedAuthenticationMeans, providerKey)
                .organizationId(typedAuthenticationMeans.get(ORGANIZATION_ID_NAME).getValue())
                .build();
    }

    private static DefaultAuthMeansBuilder prepareDefaultAuthMeansBuilder(final Map<String, BasicAuthenticationMean> typedAuthenticationMeans,
                                                                          final String providerKey) {
        return DefaultAuthMeans.builder()
                .clientId(typedAuthenticationMeans.get(CLIENT_ID_NAME).getValue())
                .institutionId(typedAuthenticationMeans.get(INSTITUTION_ID_NAME).getValue())
                .softwareId(typedAuthenticationMeans.get(SOFTWARE_ID_NAME).getValue())
                .signingKeyIdHeader(typedAuthenticationMeans.get(PRIVATE_SIGNING_KEY_HEADER_ID_NAME).getValue())
                .signingPrivateKeyId(UUID.fromString(typedAuthenticationMeans.get(SIGNING_PRIVATE_KEY_ID_NAME).getValue()))
                .transportCertificate(createCertificate(typedAuthenticationMeans, providerKey))
                .transportPrivateKeyId(UUID.fromString(typedAuthenticationMeans.get(TRANSPORT_PRIVATE_KEY_ID_NAME).getValue()));
    }

    private static X509Certificate createCertificate(final Map<String, BasicAuthenticationMean> typedAuthenticationMeans,
                                                     final String providerKey) {
        try {
            return KeyUtil.createCertificateFromPemFormat(typedAuthenticationMeans.get(HsbcGroupAuthMeansBuilderV3.TRANSPORT_CERTIFICATE_NAME).getValue());
        } catch (CertificateException e) {
            throw new InvalidAuthenticationMeansException(providerKey, HsbcGroupAuthMeansBuilderV3.TRANSPORT_CERTIFICATE_NAME, "Cannot process certificate from PEM format");
        }
    }
}
