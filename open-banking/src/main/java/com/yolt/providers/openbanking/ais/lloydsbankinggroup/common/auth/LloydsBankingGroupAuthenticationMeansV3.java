package com.yolt.providers.openbanking.ais.lloydsbankinggroup.common.auth;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.exception.InvalidAuthenticationMeansException;
import com.yolt.providers.common.util.KeyUtil;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;


@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LloydsBankingGroupAuthenticationMeansV3 {

    public static final String INSTITUTION_ID_NAME = "institution-id-2";
    public static final String CLIENT_ID_NAME = "client-id-2";
    public static final String SIGNING_KEY_HEADER_ID_NAME = "private-signing-key-header-id-2";
    public static final String TRANSPORT_CERTIFICATE_NAME = "transport-certificate-2";
    public static final String ORGANIZATION_ID_NAME = "organization-id-2";
    public static final String SOFTWARE_ID_NAME = "software-id-2";
    public static final String TRANSPORT_PRIVATE_KEY_ID_NAME = "transport-private-key-id-2";
    public static final String SIGNING_PRIVATE_KEY_ID_NAME = "signing-private-key-id-2";

    public static DefaultAuthMeans createAuthenticationMeansForAis(final Map<String, BasicAuthenticationMean> typedAuthenticationMeans, final String providerKey) {
        return createCommonAuthenticationMeans(typedAuthenticationMeans, providerKey)
                .build();
    }

    public static DefaultAuthMeans createAuthenticationMeansForPis(final Map<String, BasicAuthenticationMean> typedAuthenticationMeans, final String providerKey) {
        return createCommonAuthenticationMeans(typedAuthenticationMeans, providerKey)
                .organizationId(typedAuthenticationMeans.get(ORGANIZATION_ID_NAME).getValue())
                .softwareId(typedAuthenticationMeans.get(SOFTWARE_ID_NAME).getValue())
                .build();
    }

    private static DefaultAuthMeans.DefaultAuthMeansBuilder createCommonAuthenticationMeans(final Map<String, BasicAuthenticationMean> typedAuthenticationMeans, final String providerKey) {
        return DefaultAuthMeans.builder()
                .institutionId(typedAuthenticationMeans.get(INSTITUTION_ID_NAME).getValue())
                .clientId(typedAuthenticationMeans.get(CLIENT_ID_NAME).getValue())
                .signingKeyIdHeader(typedAuthenticationMeans.get(SIGNING_KEY_HEADER_ID_NAME).getValue())
                .signingPrivateKeyId(UUID.fromString(typedAuthenticationMeans.get(SIGNING_PRIVATE_KEY_ID_NAME).getValue()))
                .transportCertificate(createCertificate(typedAuthenticationMeans.get(TRANSPORT_CERTIFICATE_NAME).getValue(), providerKey))
                .transportPrivateKeyId(UUID.fromString(typedAuthenticationMeans.get(TRANSPORT_PRIVATE_KEY_ID_NAME).getValue()));
    }


    public static Supplier<Map<String, TypedAuthenticationMeans>> getTypedAuthenticationMeansForAIS() {
        return () -> getCommonTypedAuthenticationMeans();
    }

    public static Supplier<Map<String, TypedAuthenticationMeans>> getTypedAuthenticationMeansForPis() {
        Map<String, TypedAuthenticationMeans> typedAuthenticationMeans = getCommonTypedAuthenticationMeans();
        typedAuthenticationMeans.put(ORGANIZATION_ID_NAME, TypedAuthenticationMeans.ORGANIZATION_ID_STRING);
        typedAuthenticationMeans.put(SOFTWARE_ID_NAME, TypedAuthenticationMeans.SOFTWARE_ID_STRING);
        return () -> typedAuthenticationMeans;
    }

    public static Map<String, TypedAuthenticationMeans> getCommonTypedAuthenticationMeans() {
        Map<String, TypedAuthenticationMeans> typedAuthenticationMeans = new HashMap<>();
        typedAuthenticationMeans.put(INSTITUTION_ID_NAME, TypedAuthenticationMeans.INSTITUTION_ID_STRING);
        typedAuthenticationMeans.put(CLIENT_ID_NAME, TypedAuthenticationMeans.CLIENT_ID_STRING);
        typedAuthenticationMeans.put(SIGNING_KEY_HEADER_ID_NAME, TypedAuthenticationMeans.SIGNING_KEY_ID_STRING);
        typedAuthenticationMeans.put(TRANSPORT_CERTIFICATE_NAME, TypedAuthenticationMeans.CERTIFICATE_PEM);
        typedAuthenticationMeans.put(SIGNING_PRIVATE_KEY_ID_NAME, TypedAuthenticationMeans.KEY_ID);
        typedAuthenticationMeans.put(TRANSPORT_PRIVATE_KEY_ID_NAME, TypedAuthenticationMeans.KEY_ID);
        return typedAuthenticationMeans;
    }

    private static X509Certificate createCertificate(final String certificate, final String providerKey) {
        try {
            return KeyUtil.createCertificateFromPemFormat(certificate);
        } catch (CertificateException e) {
            throw new InvalidAuthenticationMeansException(providerKey, "transport certificate AIS or PIS", "Cannot process certificate for thumbprint");
        }
    }
}
