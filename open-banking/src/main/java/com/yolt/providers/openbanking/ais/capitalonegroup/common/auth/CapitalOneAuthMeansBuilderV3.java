package com.yolt.providers.openbanking.ais.capitalonegroup.common.auth;

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
public class CapitalOneAuthMeansBuilderV3 {

    public static final String INSTITUTION_ID_NAME = "institution-id-2";
    public static final String SOFTWARE_ID_NAME = "software-id-2";
    public static final String CLIENT_ID_NAME = "client-id-2";
    public static final String SIGNING_KEY_HEADER_ID_NAME = "private-signing-key-header-id-2";
    public static final String SIGNING_PRIVATE_KEY_ID_NAME = "private-signing-key-id-2";
    public static final String SOFTWARE_STATEMENT_ASSERTION_NAME = "software-statement-assertion-2";
    public static final String TRANSPORT_CERTIFICATE_NAME = "transport-certificate-2";
    public static final String TRANSPORT_PRIVATE_KEY_ID_NAME = "private-transport-key-id-2";
    public static final String REGISTRATION_ACCESS_TOKEN_NAME = "registration-access-token-2";

    public static DefaultAuthMeans createAuthenticationMeans(final Map<String, BasicAuthenticationMean> authenticationMeans,
                                                             final String providerKey) {
        String clientId = authenticationMeans.get(CLIENT_ID_NAME) != null ? authenticationMeans.get(CLIENT_ID_NAME).getValue() : null;
        String registrationAccessToken = authenticationMeans.get(REGISTRATION_ACCESS_TOKEN_NAME) != null ? authenticationMeans.get(REGISTRATION_ACCESS_TOKEN_NAME).getValue() : null;
        return DefaultAuthMeans.builder()
                .institutionId(authenticationMeans.get(INSTITUTION_ID_NAME).getValue())
                .clientId(clientId)
                .softwareId(authenticationMeans.get(SOFTWARE_ID_NAME).getValue())
                .signingKeyIdHeader(authenticationMeans.get(SIGNING_KEY_HEADER_ID_NAME).getValue())
                .transportCertificate(createCertificate(authenticationMeans.get(TRANSPORT_CERTIFICATE_NAME).getValue(), providerKey))
                .transportPrivateKeyId(UUID.fromString(authenticationMeans.get(TRANSPORT_PRIVATE_KEY_ID_NAME).getValue()))
                .signingPrivateKeyId(UUID.fromString(authenticationMeans.get(SIGNING_PRIVATE_KEY_ID_NAME).getValue()))
                .registrationAccessToken(registrationAccessToken)
                .build();
    }

    private static X509Certificate createCertificate(final String certificate, final String providerKey) {
        try {
            return KeyUtil.createCertificateFromPemFormat(certificate);
        } catch (CertificateException e) {
            throw new InvalidAuthenticationMeansException(providerKey, TRANSPORT_CERTIFICATE_NAME, "Cannot process certificate");
        }
    }
}
