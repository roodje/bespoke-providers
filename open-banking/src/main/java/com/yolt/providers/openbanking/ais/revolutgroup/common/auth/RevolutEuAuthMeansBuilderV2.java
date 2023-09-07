package com.yolt.providers.openbanking.ais.revolutgroup.common.auth;

import com.yolt.providers.common.domain.authenticationmeans.AuthenticationMeanType;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.types.NoWhiteCharacterStringType;
import com.yolt.providers.common.domain.authenticationmeans.types.StringType;
import com.yolt.providers.common.exception.InvalidAuthenticationMeansException;
import com.yolt.providers.common.util.KeyUtil;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static com.yolt.providers.common.domain.authenticationmeans.RenderingType.ONE_LINE_STRING;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RevolutEuAuthMeansBuilderV2 {

    public static final String INSTITUTION_ID_NAME = "institution-id";
    public static final String CLIENT_ID_NAME = "client-id";
    public static final String SIGNING_KEY_HEADER_ID_NAME = "private-signing-key-header-id";
    public static final String TRANSPORT_CERTIFICATE_NAME = "transport-certificate";
    public static final String SIGNING_PRIVATE_KEY_ID_NAME = "signing-private-key-id";
    public static final String TRANSPORT_PRIVATE_KEY_ID_NAME = "transport-private-key-id";
    public static final String ORG_JWKS_ENDPOINT_NAME = "org-jwks-endpoint";
    public static final String ORG_NAME_NAME = "org-name";
    public static final String SOFTWARE_CLIENT_NAME_NAME = "software-client-name";

    public static final TypedAuthenticationMeans ORG_JWKS_ENDPOINT_TYPE = createTypedMean("URL of public JWKs endpoint", NoWhiteCharacterStringType.getInstance());
    public static final TypedAuthenticationMeans ORG_NAME_TYPE = createTypedMean("Organization name - a name of a software (shown in UI)", StringType.getInstance());
    public static final TypedAuthenticationMeans SOFTWARE_CLIENT_NAME_TYPE = createTypedMean("Software client name - a name of a software (shown in UI)", StringType.getInstance());
    private static final String PROVIDER = "Revolut";

    public static DefaultAuthMeans createAuthenticationMeans(final Map<String, BasicAuthenticationMean> typedAuthenticationMeans) {
        return DefaultAuthMeans.builder()
                .institutionId(typedAuthenticationMeans.get(INSTITUTION_ID_NAME).getValue())
                .clientId(getNullableAuthMean(typedAuthenticationMeans, CLIENT_ID_NAME))
                .signingKeyIdHeader(typedAuthenticationMeans.get(SIGNING_KEY_HEADER_ID_NAME).getValue())
                .transportCertificate(createCertificate(typedAuthenticationMeans.get(TRANSPORT_CERTIFICATE_NAME).getValue()))
                .signingPrivateKeyId(UUID.fromString(typedAuthenticationMeans.get(SIGNING_PRIVATE_KEY_ID_NAME).getValue()))
                .transportPrivateKeyId(UUID.fromString(typedAuthenticationMeans.get(TRANSPORT_PRIVATE_KEY_ID_NAME).getValue()))
                .build();
    }

    private static X509Certificate createCertificate(final String certificate) {
        try {
            return KeyUtil.createCertificateFromPemFormat(certificate);
        } catch (CertificateException e) {
            throw new InvalidAuthenticationMeansException(PROVIDER, TRANSPORT_CERTIFICATE_NAME, "Cannot process certificate for thumbprint");
        }
    }

    private static String getNullableAuthMean(Map<String, BasicAuthenticationMean> authMeans, String key) {
        return Optional.ofNullable(authMeans.get(key)).map(BasicAuthenticationMean::getValue).orElse(null);
    }

    private static TypedAuthenticationMeans createTypedMean(String displayName, AuthenticationMeanType type) {
        return new TypedAuthenticationMeans(displayName, type, ONE_LINE_STRING);
    }
}
