package com.yolt.providers.openbanking.ais.revolutgroup.common.auth;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.InvalidAuthenticationMeansException;
import com.yolt.providers.common.util.KeyUtil;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RevolutGbAuthMeansBuilderV2 {

    public static final String INSTITUTION_ID_NAME = "institution-id-2";
    public static final String CLIENT_ID_NAME = "client-id-2";
    public static final String SIGNING_KEY_HEADER_ID_NAME = "private-signing-key-header-id-2";
    public static final String TRANSPORT_CERTIFICATE_NAME = "transport-certificate-2";
    public static final String SOFTWARE_ID_NAME = "software-id-2";
    public static final String SOFTWARE_STATEMENT_ASSERTION_NAME = "software-statement-assertion-2";
    public static final String ORGANIZATION_ID_NAME = "organization-id-2";
    public static final String SIGNING_PRIVATE_KEY_ID_NAME = "signing-private-key-id-2";
    public static final String TRANSPORT_PRIVATE_KEY_ID_NAME = "transport-private-key-id-2";

    private static final String PROVIDER = "Revolut";

    public static DefaultAuthMeans createAuthenticationMeans(final Map<String, BasicAuthenticationMean> typedAuthenticationMeans) {
        return DefaultAuthMeans.builder()
                .institutionId(typedAuthenticationMeans.get(INSTITUTION_ID_NAME).getValue())
                .clientId(getNullableAuthMean(typedAuthenticationMeans, CLIENT_ID_NAME))
                .signingKeyIdHeader(typedAuthenticationMeans.get(SIGNING_KEY_HEADER_ID_NAME).getValue())
                .transportCertificate(createCertificate(typedAuthenticationMeans.get(TRANSPORT_CERTIFICATE_NAME).getValue()))
                .softwareId(getNullableAuthMean(typedAuthenticationMeans, SOFTWARE_ID_NAME))
                .organizationId(getNullableAuthMean(typedAuthenticationMeans, ORGANIZATION_ID_NAME))
                .signingPrivateKeyId(UUID.fromString(typedAuthenticationMeans.get(SIGNING_PRIVATE_KEY_ID_NAME).getValue()))
                .transportPrivateKeyId(UUID.fromString(typedAuthenticationMeans.get(TRANSPORT_PRIVATE_KEY_ID_NAME).getValue()))
                .build();
    }

    private static X509Certificate createCertificate(final String certificate) {
        try {
            return KeyUtil.createCertificateFromPemFormat(certificate);
        } catch (CertificateException e) {
            throw new InvalidAuthenticationMeansException(PROVIDER, TRANSPORT_CERTIFICATE_NAME, "Cannot process certificate for thumbprint");
        }
    }

    private static String getNullableAuthMean(Map<String, BasicAuthenticationMean> authMeans, String key) {
        return Optional.ofNullable(authMeans.get(key)).map(BasicAuthenticationMean::getValue).orElse(null);
    }
}
