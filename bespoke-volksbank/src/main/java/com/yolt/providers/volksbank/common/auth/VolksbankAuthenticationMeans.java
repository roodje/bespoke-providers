package com.yolt.providers.volksbank.common.auth;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.exception.InvalidAuthenticationMeansException;
import com.yolt.providers.common.util.KeyUtil;
import com.yolt.providers.volksbank.common.util.HsmUtils;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Builder
@Data
public class VolksbankAuthenticationMeans {

    public static final String CLIENT_ID_NAME = "client-id";
    public static final String CLIENT_SECRET_NAME = "client-secret";
    public static final String CLIENT_TRANSPORT_CERTIFICATE_NAME = "client-transport-certificate";
    public static final String CLIENT_TRANSPORT_KEY_ID_NAME = "client-transport-private-keyid";

    @Getter
    private static final Map<String, TypedAuthenticationMeans> typedAuthenticationMeans;
    @Getter
    private static final Optional<KeyRequirements> transportKeyRequirements;
    static {
        typedAuthenticationMeans = new HashMap<>();
        typedAuthenticationMeans.put(CLIENT_ID_NAME, TypedAuthenticationMeans.CLIENT_ID_STRING);
        typedAuthenticationMeans.put(CLIENT_SECRET_NAME, TypedAuthenticationMeans.CLIENT_SECRET_STRING);
        typedAuthenticationMeans.put(CLIENT_TRANSPORT_CERTIFICATE_NAME, TypedAuthenticationMeans.CLIENT_TRANSPORT_CERTIFICATE_PEM);
        typedAuthenticationMeans.put(CLIENT_TRANSPORT_KEY_ID_NAME, TypedAuthenticationMeans.KEY_ID_HEADER_STRING);

        transportKeyRequirements = HsmUtils.getKeyRequirements(CLIENT_TRANSPORT_KEY_ID_NAME, CLIENT_TRANSPORT_CERTIFICATE_NAME);
    }

    private final String clientId;
    private final String clientSecret;
    private final UUID clientCertificateKey;
    private final X509Certificate clientCertificate;

    public static VolksbankAuthenticationMeans fromAuthenticationMeans(final Map<String, BasicAuthenticationMean> typedAuthenticationMeans,
                                                                       final String provider) {
        UUID keyId = UUID.fromString(typedAuthenticationMeans.get(CLIENT_TRANSPORT_KEY_ID_NAME).getValue());
        X509Certificate clientTransportCertificate = createCertificate(
                typedAuthenticationMeans.get(CLIENT_TRANSPORT_CERTIFICATE_NAME).getValue(), provider);

        return VolksbankAuthenticationMeans.builder()
                .clientId(typedAuthenticationMeans.get(CLIENT_ID_NAME).getValue())
                .clientSecret(typedAuthenticationMeans.get(CLIENT_SECRET_NAME).getValue())
                .clientCertificate(clientTransportCertificate)
                .clientCertificateKey(keyId)
                .build();
    }

    private static X509Certificate createCertificate(final String certificateString,
                                                     final String provider) {
        try {
            return KeyUtil.createCertificateFromPemFormat(certificateString);
        } catch (CertificateException e) {
            throw new InvalidAuthenticationMeansException(provider, CLIENT_TRANSPORT_CERTIFICATE_NAME, "Cannot process certificate for thumbprint");
        }
    }
}
