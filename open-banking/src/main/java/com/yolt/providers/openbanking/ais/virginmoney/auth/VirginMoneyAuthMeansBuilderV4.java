package com.yolt.providers.openbanking.ais.virginmoney.auth;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.InvalidAuthenticationMeansException;
import com.yolt.providers.common.util.KeyUtil;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class VirginMoneyAuthMeansBuilderV4 {

    public static final String INSTITUTION_ID_NAME = "institution-id-3";
    public static final String CLIENT_ID_NAME = "client-id-3";
    public static final String SIGNING_PRIVATE_KEY_ID_NAME = "private-signing-key-id-3";
    public static final String SIGNING_KEY_HEADER_ID_NAME = "private-signing-key-header-id-3";
    public static final String TRANSPORT_PRIVATE_KEY_ID_NAME = "private-transport-key-id-3";
    public static final String TRANSPORT_CERTIFICATE_NAME = "transport-certificate-3";
    public static final String ORGANIZATION_ID_NAME = "organization-id-3";
    public static final String SOFTWARE_ID_NAME = "software-id-3";
    public static final String SOFTWARE_STATEMENT_ASSERTION_NAME = "software-statement-assertion-3";

    private static final String PROVIDER = "VirginMoney";
    public static final String CLIENT_SECRET_NAME = "client-secret-3";

    public static DefaultAuthMeans createAuthenticationMeansForAis(final Map<String, BasicAuthenticationMean> typedAuthenticationMeans) {
        return prepareDefaultAuthMeansBuilder(typedAuthenticationMeans)
                .build();
    }

    public static DefaultAuthMeans createAuthenticationMeansForPis(final Map<String, BasicAuthenticationMean> typedAuthenticationMeans) {
        return prepareDefaultAuthMeansBuilder(typedAuthenticationMeans)
                .organizationId(typedAuthenticationMeans.get(ORGANIZATION_ID_NAME).getValue())
                .softwareId(typedAuthenticationMeans.get(SOFTWARE_ID_NAME).getValue())
                .build();
    }

    private static DefaultAuthMeans.DefaultAuthMeansBuilder prepareDefaultAuthMeansBuilder(final Map<String, BasicAuthenticationMean> typedAuthenticationMeans) {
        return DefaultAuthMeans.builder()
                .institutionId(typedAuthenticationMeans.get(INSTITUTION_ID_NAME).getValue())
                .clientId(typedAuthenticationMeans.get(CLIENT_ID_NAME) != null ? typedAuthenticationMeans.get(CLIENT_ID_NAME).getValue() : null)
                .clientSecret(typedAuthenticationMeans.get(CLIENT_SECRET_NAME) != null ? typedAuthenticationMeans.get(CLIENT_SECRET_NAME).getValue() : null)
                .signingPrivateKeyId(UUID.fromString(typedAuthenticationMeans.get(SIGNING_PRIVATE_KEY_ID_NAME).getValue()))
                .signingKeyIdHeader(typedAuthenticationMeans.get(SIGNING_KEY_HEADER_ID_NAME).getValue())
                .transportPrivateKeyId(UUID.fromString(typedAuthenticationMeans.get(TRANSPORT_PRIVATE_KEY_ID_NAME).getValue()))
                .transportCertificate(createCertificate(typedAuthenticationMeans.get(TRANSPORT_CERTIFICATE_NAME).getValue()));
    }

    private static X509Certificate createCertificate(final String certificate) {
        try {
            return KeyUtil.createCertificateFromPemFormat(certificate);
        } catch (CertificateException e) {
            throw new InvalidAuthenticationMeansException(PROVIDER, TRANSPORT_CERTIFICATE_NAME, "Cannot process certificate for thumbprint");
        }

    }
}
