package com.yolt.providers.openbanking.ais.santander.auth;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.exception.InvalidAuthenticationMeansException;
import com.yolt.providers.common.util.KeyUtil;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public class SantanderAuthMeansMapper {

    public static final String CLIENT_ID_NAME = "client-id-2";
    public static final String INSTITUTION_ID_NAME = "institution-id-2";
    public static final String PRIVATE_SIGNING_KEY_HEADER_ID_NAME = "private-signing-key-header-id-2";
    public static final String TRANSPORT_CERTIFICATE_NAME = "transport-certificate-2";
    public static final String SIGNING_PRIVATE_KEY_ID_NAME = "signing-private-key-id-2";
    public static final String TRANSPORT_PRIVATE_KEY_ID_NAME = "transport-private-key-id-2";
    public static final String SOFTWARE_ID_NAME = "software-id-2";
    public static final String ORGANIZATION_ID_NAME = "organization-id-2";

    public Function<Map<String, BasicAuthenticationMean>, DefaultAuthMeans> getAuthMeansMapperForAis(String providerKey) {
        return typedAuthenticationMeans -> mapDefaultAuthMeans(typedAuthenticationMeans, providerKey).build();
    }

    public Function<Map<String, BasicAuthenticationMean>, DefaultAuthMeans> getAuthMeansMapperForPis(String providerKey) {
        return typedAuthenticationMeans -> mapDefaultAuthMeans(typedAuthenticationMeans, providerKey)
                .softwareId(typedAuthenticationMeans.get(SOFTWARE_ID_NAME).getValue())
                .organizationId(typedAuthenticationMeans.get(ORGANIZATION_ID_NAME).getValue())
                .build();
    }

    public Map<String, TypedAuthenticationMeans> getTypedAuthMeansForAis() {
        return getDefaultTypedAuthMeans();
    }

    public Map<String, TypedAuthenticationMeans> getTypedAuthMeansForPis() {
        Map<String, TypedAuthenticationMeans> typedAuthenticationMeans = getDefaultTypedAuthMeans();
        typedAuthenticationMeans.put(SOFTWARE_ID_NAME, TypedAuthenticationMeans.SOFTWARE_ID_STRING);
        typedAuthenticationMeans.put(ORGANIZATION_ID_NAME, TypedAuthenticationMeans.ORGANIZATION_ID_STRING);
        return typedAuthenticationMeans;
    }

    private Map<String, TypedAuthenticationMeans> getDefaultTypedAuthMeans() {
        Map<String, TypedAuthenticationMeans> typedAuthenticationMeans = new HashMap<>();
        typedAuthenticationMeans.put(CLIENT_ID_NAME, TypedAuthenticationMeans.CLIENT_ID_STRING);
        typedAuthenticationMeans.put(INSTITUTION_ID_NAME, TypedAuthenticationMeans.INSTITUTION_ID_STRING);
        typedAuthenticationMeans.put(PRIVATE_SIGNING_KEY_HEADER_ID_NAME, TypedAuthenticationMeans.SIGNING_KEY_ID_STRING);
        typedAuthenticationMeans.put(TRANSPORT_CERTIFICATE_NAME, TypedAuthenticationMeans.CERTIFICATE_PEM);
        typedAuthenticationMeans.put(SIGNING_PRIVATE_KEY_ID_NAME, TypedAuthenticationMeans.KEY_ID);
        typedAuthenticationMeans.put(TRANSPORT_PRIVATE_KEY_ID_NAME, TypedAuthenticationMeans.KEY_ID);
        return typedAuthenticationMeans;
    }

    private DefaultAuthMeans.DefaultAuthMeansBuilder mapDefaultAuthMeans(Map<String, BasicAuthenticationMean> typedAuthenticationMeans,
                                                                         String providerKey) {
        return DefaultAuthMeans.builder()
                .clientId(typedAuthenticationMeans.get(CLIENT_ID_NAME).getValue())
                .institutionId(typedAuthenticationMeans.get(INSTITUTION_ID_NAME).getValue())
                .signingPrivateKeyId(UUID.fromString(typedAuthenticationMeans.get(SIGNING_PRIVATE_KEY_ID_NAME).getValue()))
                .signingKeyIdHeader(typedAuthenticationMeans.get(PRIVATE_SIGNING_KEY_HEADER_ID_NAME).getValue())
                .transportCertificate(createCertificate(typedAuthenticationMeans, providerKey))
                .transportPrivateKeyId(UUID.fromString(typedAuthenticationMeans.get(TRANSPORT_PRIVATE_KEY_ID_NAME).getValue()));
    }

    private X509Certificate createCertificate(final Map<String, BasicAuthenticationMean> typedAuthenticationMeans,
                                              final String providerKey) {
        try {
            return KeyUtil.createCertificateFromPemFormat(typedAuthenticationMeans.get(TRANSPORT_CERTIFICATE_NAME).getValue());
        } catch (CertificateException e) {
            throw new InvalidAuthenticationMeansException(providerKey, TRANSPORT_CERTIFICATE_NAME, "Cannot process certificate from PEM format");
        }
    }
}
