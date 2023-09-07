package com.yolt.providers.openbanking.ais.bankofirelandgroup.bankofirelandroi.auth;

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

public class BankOfIrelandRoiAuthMeansMapper {

    public static final String SOFTWARE_ID_NAME = "software-id";
    public static final String INSTITUTION_ID_NAME = "institution-id";
    public static final String SOFTWARE_STATEMENT_ASSERTION_NAME = "software-statement-assertion";
    public static final String CLIENT_ID_NAME = "client-id";
    public static final String SIGNING_KEY_HEADER_ID_NAME = "private-signing-key-header-id";
    public static final String SIGNING_PRIVATE_KEY_ID_NAME = "signing-private-key-id";
    public static final String TRANSPORT_CERTIFICATE_CHAIN_NAME = "transport-certificate-chain";
    public static final String TRANSPORT_PRIVATE_KEY_ID_NAME = "transport-private-key-id";

    public Function<Map<String, BasicAuthenticationMean>, DefaultAuthMeans> getAuthMeansMapper(String providerKey) {
        return typedAuthenticationMeans -> DefaultAuthMeans.builder()
                .institutionId(typedAuthenticationMeans.get(INSTITUTION_ID_NAME).getValue())
                .clientId(getOptionalAuthenticationMeanValue(typedAuthenticationMeans, CLIENT_ID_NAME))
                .signingKeyIdHeader(typedAuthenticationMeans.get(SIGNING_KEY_HEADER_ID_NAME).getValue())
                .signingPrivateKeyId(UUID.fromString(typedAuthenticationMeans.get(SIGNING_PRIVATE_KEY_ID_NAME).getValue()))
                .transportCertificatesChain(createCertificateChain(typedAuthenticationMeans.get(TRANSPORT_CERTIFICATE_CHAIN_NAME).getValue(), providerKey))
                .transportPrivateKeyId(UUID.fromString(typedAuthenticationMeans.get(TRANSPORT_PRIVATE_KEY_ID_NAME).getValue()))
                .build();
    }

    public Map<String, TypedAuthenticationMeans> getTypedAuthMeans() {
        Map<String, TypedAuthenticationMeans> typedAuthenticationMeans = new HashMap<>();
        typedAuthenticationMeans.put(SOFTWARE_ID_NAME, TypedAuthenticationMeans.SOFTWARE_ID_STRING);
        typedAuthenticationMeans.put(INSTITUTION_ID_NAME, TypedAuthenticationMeans.INSTITUTION_ID_STRING);
        typedAuthenticationMeans.put(SOFTWARE_STATEMENT_ASSERTION_NAME, TypedAuthenticationMeans.SOFTWARE_STATEMENT_ASSERTION_STRING);
        typedAuthenticationMeans.put(CLIENT_ID_NAME, TypedAuthenticationMeans.CLIENT_ID_STRING);
        typedAuthenticationMeans.put(SIGNING_KEY_HEADER_ID_NAME, TypedAuthenticationMeans.SIGNING_KEY_ID_STRING);
        typedAuthenticationMeans.put(TRANSPORT_CERTIFICATE_CHAIN_NAME, TypedAuthenticationMeans.CERTIFICATE_PEM);
        typedAuthenticationMeans.put(SIGNING_PRIVATE_KEY_ID_NAME, TypedAuthenticationMeans.KEY_ID);
        typedAuthenticationMeans.put(TRANSPORT_PRIVATE_KEY_ID_NAME, TypedAuthenticationMeans.KEY_ID);
        return typedAuthenticationMeans;
    }

    private X509Certificate[] createCertificateChain(String certificate, String providerKey) {
        try {
            return KeyUtil.createCertificatesChainFromPemFormat(certificate);
        } catch (CertificateException e) {
            throw new InvalidAuthenticationMeansException(providerKey, TRANSPORT_CERTIFICATE_CHAIN_NAME, "Cannot process certificate for thumbprint");
        }
    }

    private String getOptionalAuthenticationMeanValue(Map<String, BasicAuthenticationMean> authenticationMeansMap,
                                                      String key) {
        return authenticationMeansMap.get(key) == null ? null : authenticationMeansMap.get(key).getValue();
    }
}
