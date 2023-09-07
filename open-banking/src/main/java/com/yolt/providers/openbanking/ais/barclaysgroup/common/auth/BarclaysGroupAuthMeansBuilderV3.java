package com.yolt.providers.openbanking.ais.barclaysgroup.common.auth;

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
public class BarclaysGroupAuthMeansBuilderV3 {
    public static final String CLIENT_ID_NAME_V2 = "client_id_v2";
    public static final String INSTITUTION_ID_NAME_V2 = "institution_id_v2";
    public static final String PRIVATE_SIGNING_KEY_HEADER_ID_NAME_V2 = "private_signing_key_header_id_v2";
    public static final String TRANSPORT_CERTIFICATE_NAME_V2 = "transport_certificate_v2";
    public static final String ORGANIZATION_ID_NAME_V2 = "organization_id_v2";
    public static final String SOFTWARE_ID_NAME_V2 = "software_id_v2";
    public static final String SIGNING_PRIVATE_KEY_ID_NAME_V2 = "signing_private_key_id_v2";
    public static final String TRANSPORT_PRIVATE_KEY_ID_NAME_V2 = "transport_private_key_id_v2";

    public static DefaultAuthMeans createAuthenticationMeans(final Map<String, BasicAuthenticationMean> typedAuthenticationMeans) {
        return DefaultAuthMeans.builder()
                .clientId(typedAuthenticationMeans.get(CLIENT_ID_NAME_V2).getValue())
                .institutionId(typedAuthenticationMeans.get(INSTITUTION_ID_NAME_V2).getValue())
                .signingKeyIdHeader(typedAuthenticationMeans.get(PRIVATE_SIGNING_KEY_HEADER_ID_NAME_V2).getValue())
                .signingPrivateKeyId(UUID.fromString(typedAuthenticationMeans.get(SIGNING_PRIVATE_KEY_ID_NAME_V2).getValue()))
                .transportCertificate(createCertificate(typedAuthenticationMeans))
                .organizationId(typedAuthenticationMeans.get(ORGANIZATION_ID_NAME_V2).getValue())
                .softwareId(typedAuthenticationMeans.get(SOFTWARE_ID_NAME_V2).getValue())
                .transportPrivateKeyId(UUID.fromString(typedAuthenticationMeans.get(TRANSPORT_PRIVATE_KEY_ID_NAME_V2).getValue()))
                .build();
    }

    public static Supplier<Map<String, TypedAuthenticationMeans>> getTypedAuthenticationMeans() {
        Map<String, TypedAuthenticationMeans> typedAuthenticationMeans = new HashMap<>();
        typedAuthenticationMeans.put(CLIENT_ID_NAME_V2, TypedAuthenticationMeans.CLIENT_ID_STRING);
        typedAuthenticationMeans.put(INSTITUTION_ID_NAME_V2, TypedAuthenticationMeans.INSTITUTION_ID_STRING);
        typedAuthenticationMeans.put(PRIVATE_SIGNING_KEY_HEADER_ID_NAME_V2, TypedAuthenticationMeans.SIGNING_KEY_ID_STRING);
        typedAuthenticationMeans.put(TRANSPORT_CERTIFICATE_NAME_V2, TypedAuthenticationMeans.CERTIFICATE_PEM);
        typedAuthenticationMeans.put(ORGANIZATION_ID_NAME_V2, TypedAuthenticationMeans.ORGANIZATION_ID_STRING);
        typedAuthenticationMeans.put(SOFTWARE_ID_NAME_V2, TypedAuthenticationMeans.SOFTWARE_ID_STRING);
        typedAuthenticationMeans.put(SIGNING_PRIVATE_KEY_ID_NAME_V2, TypedAuthenticationMeans.KEY_ID);
        typedAuthenticationMeans.put(TRANSPORT_PRIVATE_KEY_ID_NAME_V2, TypedAuthenticationMeans.KEY_ID);
        return () -> typedAuthenticationMeans;
    }

    private static X509Certificate createCertificate(final Map<String, BasicAuthenticationMean> typedAuthenticationMeans) {
        try {
            return KeyUtil.createCertificateFromPemFormat(typedAuthenticationMeans.get(TRANSPORT_CERTIFICATE_NAME_V2).getValue());
        } catch (CertificateException e) {
            throw new InvalidAuthenticationMeansException("Barclays", TRANSPORT_CERTIFICATE_NAME_V2, "Cannot process certificate for thumbprint");
        }
    }
}
