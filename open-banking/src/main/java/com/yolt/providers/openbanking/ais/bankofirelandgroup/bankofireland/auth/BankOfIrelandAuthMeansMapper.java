package com.yolt.providers.openbanking.ais.bankofirelandgroup.bankofireland.auth;

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

public class BankOfIrelandAuthMeansMapper {

    public static final String SOFTWARE_ID_NAME_V2 = "software-id-2";
    public static final String INSTITUTION_ID_NAME_V2 = "institution-id-2";
    public static final String SOFTWARE_STATEMENT_ASSERTION_NAME_V2 = "software-statement-assertion-2";
    public static final String CLIENT_ID_NAME_V2 = "client-id-2";
    public static final String SIGNING_KEY_HEADER_ID_NAME_V2 = "private-signing-key-header-id-2";
    public static final String SIGNING_PRIVATE_KEY_ID_NAME_V2 = "signing-private-key-id-2";
    public static final String TRANSPORT_CERTIFICATE_NAME_V2 = "transport-certificate-2";
    public static final String TRANSPORT_PRIVATE_KEY_ID_NAME_V2 = "transport-private-key-id-2";

    public Function<Map<String, BasicAuthenticationMean>, DefaultAuthMeans> getAuthMeansMapper(String providerKey) {
        return typedAuthenticationMeans -> DefaultAuthMeans.builder()
                .institutionId(typedAuthenticationMeans.get(INSTITUTION_ID_NAME_V2).getValue())
                .clientId(getOptionalAuthenticationMeanValue(typedAuthenticationMeans, CLIENT_ID_NAME_V2))
                .signingKeyIdHeader(typedAuthenticationMeans.get(SIGNING_KEY_HEADER_ID_NAME_V2).getValue())
                .signingPrivateKeyId(UUID.fromString(typedAuthenticationMeans.get(SIGNING_PRIVATE_KEY_ID_NAME_V2).getValue()))
                .transportCertificate(createCertificate(typedAuthenticationMeans.get(TRANSPORT_CERTIFICATE_NAME_V2).getValue(), providerKey))
                .transportPrivateKeyId(UUID.fromString(typedAuthenticationMeans.get(TRANSPORT_PRIVATE_KEY_ID_NAME_V2).getValue()))
                .softwareId(typedAuthenticationMeans.get(SOFTWARE_ID_NAME_V2).getValue())
                .build();
    }

    public Map<String, TypedAuthenticationMeans> getTypedAuthMeans() {
        Map<String, TypedAuthenticationMeans> typedAuthenticationMeans = new HashMap<>();
        typedAuthenticationMeans.put(SOFTWARE_ID_NAME_V2, TypedAuthenticationMeans.SOFTWARE_ID_STRING);
        typedAuthenticationMeans.put(INSTITUTION_ID_NAME_V2, TypedAuthenticationMeans.INSTITUTION_ID_STRING);
        typedAuthenticationMeans.put(SOFTWARE_STATEMENT_ASSERTION_NAME_V2, TypedAuthenticationMeans.SOFTWARE_STATEMENT_ASSERTION_STRING);
        typedAuthenticationMeans.put(CLIENT_ID_NAME_V2, TypedAuthenticationMeans.CLIENT_ID_STRING);
        typedAuthenticationMeans.put(SIGNING_KEY_HEADER_ID_NAME_V2, TypedAuthenticationMeans.SIGNING_KEY_ID_STRING);
        typedAuthenticationMeans.put(TRANSPORT_CERTIFICATE_NAME_V2, TypedAuthenticationMeans.CERTIFICATE_PEM);
        typedAuthenticationMeans.put(SIGNING_PRIVATE_KEY_ID_NAME_V2, TypedAuthenticationMeans.KEY_ID);
        typedAuthenticationMeans.put(TRANSPORT_PRIVATE_KEY_ID_NAME_V2, TypedAuthenticationMeans.KEY_ID);
        return typedAuthenticationMeans;
    }

    private X509Certificate createCertificate(String certificate, String providerKey) {
        try {
            return KeyUtil.createCertificateFromPemFormat(certificate);
        } catch (CertificateException e) {
            throw new InvalidAuthenticationMeansException(providerKey, TRANSPORT_CERTIFICATE_NAME_V2, "Cannot process certificate for thumbprint");
        }
    }

    private String getOptionalAuthenticationMeanValue(Map<String, BasicAuthenticationMean> authenticationMeansMap,
                                                      String key) {
        return authenticationMeansMap.get(key) == null ? null : authenticationMeansMap.get(key).getValue();
    }
}
