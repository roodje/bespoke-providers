package com.yolt.providers.openbanking.ais.vanquisgroup.common.auth;

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
public class VanquisGroupAuthMeansBuilderV2 {

    public static final String INSTITUTION_ID_NAME = "institution-id";
    public static final String CLIENT_ID_NAME = "client-id";
    public static final String SIGNING_PRIVATE_KEY_ID_NAME = "private-signing-key-id";
    public static final String SIGNING_KEY_HEADER_ID_NAME = "private-signing-key-header-id";
    public static final String TRANSPORT_PRIVATE_KEY_ID_NAME = "private-transport-key-id";
    public static final String TRANSPORT_CERTIFICATE_NAME = "transport-certificate";
    public static final String SOFTWARE_ID_NAME = "software-id";
    public static final String SOFTWARE_STATEMENT_ASSERTION_NAME = "software-statement-assertion";

    public static DefaultAuthMeans createAuthenticationMeans(final Map<String, BasicAuthenticationMean> typedAuthenticationMeans,
                                                             final String providerName) {
        String clientId = null;

        if (typedAuthenticationMeans.containsKey(CLIENT_ID_NAME)) {
            clientId = typedAuthenticationMeans.get(CLIENT_ID_NAME).getValue();
        }

        return DefaultAuthMeans.builder()
                .institutionId(typedAuthenticationMeans.get(INSTITUTION_ID_NAME).getValue())
                .clientId(clientId)
                .signingKeyIdHeader(typedAuthenticationMeans.get(SIGNING_KEY_HEADER_ID_NAME).getValue())
                .transportCertificate(createCertificate(typedAuthenticationMeans.get(TRANSPORT_CERTIFICATE_NAME).getValue(), providerName))
                .softwareId(typedAuthenticationMeans.get(SOFTWARE_ID_NAME).getValue())
                .transportPrivateKeyId(UUID.fromString(typedAuthenticationMeans.get(TRANSPORT_PRIVATE_KEY_ID_NAME).getValue()))
                .signingPrivateKeyId(UUID.fromString(typedAuthenticationMeans.get(SIGNING_PRIVATE_KEY_ID_NAME).getValue()))
                .build();
    }

    private static X509Certificate createCertificate(final String certificate, final String providerName) {
        try {
            return KeyUtil.createCertificateFromPemFormat(certificate);
        } catch (CertificateException e) {
            throw new InvalidAuthenticationMeansException(providerName, TRANSPORT_CERTIFICATE_NAME, "Cannot process certificate for thumbprint");
        }
    }
}