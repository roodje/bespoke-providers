package com.yolt.providers.openbanking.ais.cybgroup.common.auth;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.InvalidAuthenticationMeansException;
import com.yolt.providers.common.util.KeyUtil;
import com.yolt.providers.openbanking.ais.cybgroup.common.config.CybgGroupPropertiesV2;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CybgGroupAuthMeansBuilderV2 {

    public static final String INSTITUTION_ID_NAME = "institution-id";
    public static final String SOFTWARE_ID_NAME = "software-id";
    public static final String CLIENT_ID_NAME = "client-id";
    public static final String CLIENT_SECRET_NAME = "client-secret";
    public static final String SIGNING_KEY_HEADER_ID_NAME = "private-signing-key-header-id";
    public static final String SIGNING_PRIVATE_KEY_ID_NAME = "private-signing-key-id";
    public static final String SOFTWARE_STATEMENT_ASSERTION_NAME = "software-statement-assertion";
    public static final String TRANSPORT_CERTIFICATE_NAME = "transport-certificate";
    public static final String TRANSPORT_PRIVATE_KEY_ID_NAME = "private-transport-key-id";

    public static DefaultAuthMeans createAuthenticationMeans(final Map<String, BasicAuthenticationMean> authenticationMeans,
                                                             final String providerKey,
                                                             final CybgGroupPropertiesV2 properties) {
        return DefaultAuthMeans.builder()
                .institutionId(properties.getInstitutionId())
                .clientId(authenticationMeans.get(CLIENT_ID_NAME).getValue())
                .clientSecret(authenticationMeans.get(CLIENT_SECRET_NAME).getValue())
                .softwareId(authenticationMeans.get(SOFTWARE_ID_NAME).getValue())
                .signingKeyIdHeader(authenticationMeans.get(SIGNING_KEY_HEADER_ID_NAME).getValue())
                .transportCertificate(createCertificate(authenticationMeans.get(TRANSPORT_CERTIFICATE_NAME).getValue(), providerKey))
                .transportPrivateKeyId(mapToUUID(authenticationMeans.get(TRANSPORT_PRIVATE_KEY_ID_NAME).getValue()))
                .signingPrivateKeyId(mapToUUID(authenticationMeans.get(SIGNING_PRIVATE_KEY_ID_NAME).getValue()))
                .build();
    }

    private static UUID mapToUUID(String value) {
        return value == null ? null : UUID.fromString(value);
    }

    private static X509Certificate createCertificate(final String certificate, final String providerKey) {
        try {
            return KeyUtil.createCertificateFromPemFormat(certificate);
        } catch (CertificateException e) {
            throw new InvalidAuthenticationMeansException(providerKey, TRANSPORT_CERTIFICATE_NAME, "Cannot process certificate for thumbprint");
        }
    }
}
