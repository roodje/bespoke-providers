package com.yolt.providers.openbanking.ais.danske.oauth2;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.exception.InvalidAuthenticationMeansException;
import com.yolt.providers.common.util.KeyUtil;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DanskeAuthMeansBuilderV3 {

    public static final String SOFTWARE_ID_NAME = "software-id-2";
    public static final String SOFTWARE_STATEMENT_ASSERTION_NAME = "software-statement-assertion-2";
    public static final String INSTITUTION_ID_NAME = "institution-id-2";
    public static final String CLIENT_ID_NAME = "client-id-2";
    public static final String SIGNING_KEY_HEADER_ID_NAME = "private-signing-key-header-id-2";
    public static final String SIGNING_PRIVATE_KEY_ID_NAME = "signing-private-key-id-2";
    public static final String TRANSPORT_CERTIFICATE_NAME = "transport-certificate-2";
    public static final String TRANSPORT_PRIVATE_KEY_ID_NAME = "transport-private-key-id-2";
    public static final String ORGANIZATION_ID_NAME = "organization-id-2";

    public static DefaultAuthMeans createDefaultAuthenticationMeans(final Map<String, BasicAuthenticationMean> typedAuthenticationMeans, String providerIdentifier) {
        return prepareCommonAuthMeansBuilder(typedAuthenticationMeans, providerIdentifier)
                .build();
    }

    public static DefaultAuthMeans createDefaultAuthenticationMeansForPIS(final Map<String, BasicAuthenticationMean> typedAuthenticationMeans, String providerIdentifier) {
        return prepareCommonAuthMeansBuilder(typedAuthenticationMeans, providerIdentifier)
                .organizationId(typedAuthenticationMeans.get(ORGANIZATION_ID_NAME).getValue())
                .softwareId(typedAuthenticationMeans.get(SOFTWARE_ID_NAME).getValue())
                .build();
    }

    public static DefaultAuthMeans.DefaultAuthMeansBuilder prepareCommonAuthMeansBuilder(final Map<String, BasicAuthenticationMean> typedAuthenticationMeans, String providerIdentifier) {
        String clientId = getOptionalAuthenticationMeanValue(typedAuthenticationMeans, CLIENT_ID_NAME);
        return DefaultAuthMeans.builder()
                .institutionId(typedAuthenticationMeans.get(INSTITUTION_ID_NAME).getValue())
                .clientId(clientId)
                .signingKeyIdHeader(typedAuthenticationMeans.get(SIGNING_KEY_HEADER_ID_NAME).getValue())
                .transportCertificate(createCertificate(typedAuthenticationMeans.get(TRANSPORT_CERTIFICATE_NAME).getValue(), providerIdentifier))
                .transportPrivateKeyId(UUID.fromString(typedAuthenticationMeans.get(TRANSPORT_PRIVATE_KEY_ID_NAME).getValue()))
                .signingPrivateKeyId(UUID.fromString(typedAuthenticationMeans.get(SIGNING_PRIVATE_KEY_ID_NAME).getValue()));
    }

    public static Supplier<Map<String, TypedAuthenticationMeans>> getTypedAuthenticationMeansForAIS() {
        Map<String, TypedAuthenticationMeans> typedAuthenticationMeans = getCommonTypedAuthMeans();
        return () -> typedAuthenticationMeans;
    }

    public static Supplier<Map<String, TypedAuthenticationMeans>> getTypedAuthenticationMeansForPIS() {
        Map<String, TypedAuthenticationMeans> typedAuthenticationMeans = getCommonTypedAuthMeans();
        typedAuthenticationMeans.put(ORGANIZATION_ID_NAME, TypedAuthenticationMeans.ORGANIZATION_ID_STRING);
        return () -> typedAuthenticationMeans;
    }

    private static Map<String, TypedAuthenticationMeans> getCommonTypedAuthMeans() {
        Map<String, TypedAuthenticationMeans> typedAuthenticationMeans = new HashMap<>();
        typedAuthenticationMeans.put(SOFTWARE_ID_NAME, TypedAuthenticationMeans.SOFTWARE_ID_STRING);
        typedAuthenticationMeans.put(INSTITUTION_ID_NAME, TypedAuthenticationMeans.INSTITUTION_ID_STRING);
        typedAuthenticationMeans.put(SOFTWARE_STATEMENT_ASSERTION_NAME, TypedAuthenticationMeans.SOFTWARE_STATEMENT_ASSERTION_STRING);
        typedAuthenticationMeans.put(CLIENT_ID_NAME, TypedAuthenticationMeans.CLIENT_ID_STRING);
        typedAuthenticationMeans.put(SIGNING_KEY_HEADER_ID_NAME, TypedAuthenticationMeans.SIGNING_KEY_ID_STRING);
        typedAuthenticationMeans.put(SIGNING_PRIVATE_KEY_ID_NAME, TypedAuthenticationMeans.KEY_ID);
        typedAuthenticationMeans.put(TRANSPORT_CERTIFICATE_NAME, TypedAuthenticationMeans.CERTIFICATE_PEM);
        typedAuthenticationMeans.put(TRANSPORT_PRIVATE_KEY_ID_NAME, TypedAuthenticationMeans.KEY_ID);
        return typedAuthenticationMeans;
    }

    private static X509Certificate createCertificate(final String certificate, String providerIdentifier) {
        try {
            if (!StringUtils.isEmpty(certificate))
                return KeyUtil.createCertificateFromPemFormat(certificate);
            else {
                return null;
            }
        } catch (CertificateException e) {
            throw new InvalidAuthenticationMeansException(providerIdentifier, TRANSPORT_CERTIFICATE_NAME, "Cannot process certificate for thumbprint");
        }
    }

    private static String getOptionalAuthenticationMeanValue(final Map<String, BasicAuthenticationMean> authenticationMeansMap,
                                                             final String key) {
        return authenticationMeansMap.get(key) == null ? null : authenticationMeansMap.get(key).getValue();
    }
}
