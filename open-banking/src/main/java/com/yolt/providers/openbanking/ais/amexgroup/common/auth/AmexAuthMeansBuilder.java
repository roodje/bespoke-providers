package com.yolt.providers.openbanking.ais.amexgroup.common.auth;

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
import java.util.function.Function;
import java.util.function.Supplier;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AmexAuthMeansBuilder {

    public static final String CLIENT_ID = "client-id-2";
    public static final String CLIENT_SECRET = "client-secret-2";
    public static final String CLIENT_TRANSPORT_KEY_ID_ROTATION = "transport-private-kid-2";
    public static final String CLIENT_TRANSPORT_CERTIFICATE_ROTATION = "transport-certificate-2";

    private static final String PROVIDER = "AMEX";

    public static Function<Map<String, BasicAuthenticationMean>, DefaultAuthMeans> createAuthenticationMeansFunction() {
        return typed -> DefaultAuthMeans.builder()
                .clientId(typed.get(CLIENT_ID).getValue())
                .clientSecret(typed.get(CLIENT_SECRET).getValue())
                .transportPrivateKeyId(UUID.fromString(typed.get(CLIENT_TRANSPORT_KEY_ID_ROTATION).getValue()))
                .transportCertificate(createCertificate(typed.get(CLIENT_TRANSPORT_CERTIFICATE_ROTATION).getValue()))
                .build();
    }

    public static Supplier<Map<String, TypedAuthenticationMeans>> getTypedAuthenticationMeans() {
        Map<String, TypedAuthenticationMeans> typedAuthenticationMeans = new HashMap<>();
        typedAuthenticationMeans.put(CLIENT_ID, TypedAuthenticationMeans.CLIENT_ID_STRING);
        typedAuthenticationMeans.put(CLIENT_SECRET, TypedAuthenticationMeans.CLIENT_SECRET_STRING);
        typedAuthenticationMeans.put(CLIENT_TRANSPORT_KEY_ID_ROTATION, TypedAuthenticationMeans.KEY_ID);
        typedAuthenticationMeans.put(CLIENT_TRANSPORT_CERTIFICATE_ROTATION, TypedAuthenticationMeans.CLIENT_TRANSPORT_CERTIFICATE_PEM);

        return () -> typedAuthenticationMeans;
    }

    private static X509Certificate createCertificate(final String certificate) {
        try {
            return KeyUtil.createCertificateFromPemFormat(certificate);
        } catch (CertificateException e) {
            throw new InvalidAuthenticationMeansException(PROVIDER, CLIENT_TRANSPORT_CERTIFICATE_ROTATION, "Cannot process certificate for thumbprint");
        }
    }
}
