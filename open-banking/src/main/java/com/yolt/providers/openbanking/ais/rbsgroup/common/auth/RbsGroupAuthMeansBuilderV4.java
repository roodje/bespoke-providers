package com.yolt.providers.openbanking.ais.rbsgroup.common.auth;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.exception.InvalidAuthenticationMeansException;
import com.yolt.providers.common.util.KeyUtil;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RbsGroupAuthMeansBuilderV4 {

    public static final String INSTITUTION_ID_NAME = "institution-id-2";
    public static final String CLIENT_ID_NAME = "client-id-2";
    public static final String SIGNING_KEY_HEADER_ID_NAME = "signing-key-header-id-2";
    public static final String TRANSPORT_CERTIFICATES_CHAIN_NAME = "transport-certificates-chain-2";
    public static final String SOFTWARE_ID_NAME = "software-id-2";
    public static final String SOFTWARE_STATEMENT_ASSERTION_NAME = "software-statement-assertion-2";
    public static final String ORGANIZATION_ID_NAME = "organization-id-2";
    public static final String SIGNING_PRIVATE_KEY_ID_NAME = "signing-private-key-id-2";
    public static final String TRANSPORT_PRIVATE_KEY_ID_NAME = "transport-private-key-id-2";

    public static DefaultAuthMeans createAuthenticationMeansForAis(final String providerIdentifierDisplayName,
                                                                   final Map<String, BasicAuthenticationMean> typedAuthenticationMeans) {
        return prepareDefaultAuthMeansBuilder(providerIdentifierDisplayName, typedAuthenticationMeans)
                .build();
    }

    public static DefaultAuthMeans createAuthenticationMeansForPis(final String providerIdentifierDisplayName,
                                                                   final Map<String, BasicAuthenticationMean> typedAuthenticationMeans) {
        return prepareDefaultAuthMeansBuilder(providerIdentifierDisplayName, typedAuthenticationMeans)
                .softwareId(typedAuthenticationMeans.get(SOFTWARE_ID_NAME).getValue())
                .organizationId(typedAuthenticationMeans.get(ORGANIZATION_ID_NAME).getValue())
                .build();
    }

    private static DefaultAuthMeans.DefaultAuthMeansBuilder prepareDefaultAuthMeansBuilder(String providerIdentifierDisplayName,
                                                                                           Map<String, BasicAuthenticationMean> typedAuthenticationMeans) {

        return DefaultAuthMeans.builder()
                .institutionId(typedAuthenticationMeans.get(INSTITUTION_ID_NAME).getValue())
                .clientId(getOptionalAuthenticationMeanValue(typedAuthenticationMeans, CLIENT_ID_NAME))
                .signingKeyIdHeader(typedAuthenticationMeans.get(SIGNING_KEY_HEADER_ID_NAME).getValue())
                .signingPrivateKeyId(UUID.fromString(typedAuthenticationMeans.get(SIGNING_PRIVATE_KEY_ID_NAME).getValue()))
                .transportCertificatesChain(createCertificatesChain(providerIdentifierDisplayName, typedAuthenticationMeans.get(TRANSPORT_CERTIFICATES_CHAIN_NAME).getValue()))
                .transportPrivateKeyId(UUID.fromString(typedAuthenticationMeans.get(TRANSPORT_PRIVATE_KEY_ID_NAME).getValue()));
    }

    public static Supplier<Map<String, TypedAuthenticationMeans>> getTypedAuthenticationMeans() {
        Map<String, TypedAuthenticationMeans> typedAuthenticationMeans = new HashMap<>();
        typedAuthenticationMeans.put(INSTITUTION_ID_NAME, TypedAuthenticationMeans.INSTITUTION_ID_STRING);
        typedAuthenticationMeans.put(CLIENT_ID_NAME, TypedAuthenticationMeans.CLIENT_ID_STRING);
        typedAuthenticationMeans.put(SIGNING_KEY_HEADER_ID_NAME, TypedAuthenticationMeans.SIGNING_KEY_ID_STRING);
        typedAuthenticationMeans.put(TRANSPORT_CERTIFICATES_CHAIN_NAME, TypedAuthenticationMeans.CLIENT_TRANSPORT_CERTIFICATES_CHAIN_PEM);
        typedAuthenticationMeans.put(SOFTWARE_ID_NAME, TypedAuthenticationMeans.SOFTWARE_ID_STRING);
        typedAuthenticationMeans.put(SOFTWARE_STATEMENT_ASSERTION_NAME, TypedAuthenticationMeans.SOFTWARE_STATEMENT_ASSERTION_STRING);
        typedAuthenticationMeans.put(ORGANIZATION_ID_NAME, TypedAuthenticationMeans.ORGANIZATION_ID_STRING);
        typedAuthenticationMeans.put(SIGNING_PRIVATE_KEY_ID_NAME, TypedAuthenticationMeans.KEY_ID);
        typedAuthenticationMeans.put(TRANSPORT_PRIVATE_KEY_ID_NAME, TypedAuthenticationMeans.KEY_ID);
        return () -> typedAuthenticationMeans;
    }

    private static String getOptionalAuthenticationMeanValue(final Map<String, BasicAuthenticationMean> authenticationMeansMap,
                                                             final String key) {
        return authenticationMeansMap.containsKey(key) ? authenticationMeansMap.get(key).getValue() : null;
    }

    private static X509Certificate[] createCertificatesChain(String providerIdentifier, String transportCertificatesChain) {
        try {
            return KeyUtil.createCertificatesChainFromPemFormat(transportCertificatesChain);
        } catch (CertificateException e) {
            throw new InvalidAuthenticationMeansException(providerIdentifier, TRANSPORT_CERTIFICATES_CHAIN_NAME,
                    "Cannot deserialize transport certificates chain");
        }
    }
}
