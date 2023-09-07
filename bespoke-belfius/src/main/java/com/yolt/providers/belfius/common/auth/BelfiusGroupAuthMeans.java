package com.yolt.providers.belfius.common.auth;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.InvalidAuthenticationMeansException;
import com.yolt.providers.common.util.KeyUtil;
import lombok.Builder;
import lombok.Data;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.UUID;

@Builder
@Data
public class BelfiusGroupAuthMeans {

    public static final String CLIENT_ID_NAME = "client-id";
    public static final String CLIENT_SECRET_NAME = "client-secret";
    public static final String CLIENT_TRANSPORT_CERTIFICATE_NAME = "client-transport-certificate";
    public static final String CLIENT_TRANSPORT_KEY_ID_NAME = "client-transport-private-keyid";

    private final String clientId;
    private final String clientSecret;
    private final UUID clientCertificateKey;
    private final X509Certificate clientCertificate;

    public static BelfiusGroupAuthMeans fromAuthenticationMeans(Map<String, BasicAuthenticationMean> authenticationMeans,
                                                                String provider) {
        X509Certificate clientTransportCertificate = createCertificate(
                authenticationMeans.get(CLIENT_TRANSPORT_CERTIFICATE_NAME).getValue(), provider);

        return BelfiusGroupAuthMeans.builder()
                .clientId(authenticationMeans.get(CLIENT_ID_NAME).getValue())
                .clientSecret(authenticationMeans.get(CLIENT_SECRET_NAME).getValue())
                .clientCertificate(clientTransportCertificate)
                .clientCertificateKey(UUID.fromString(authenticationMeans.get(CLIENT_TRANSPORT_KEY_ID_NAME).getValue()))
                .build();
    }

    private static X509Certificate createCertificate(String certificateString,
                                                     String provider) {
        try {
            return KeyUtil.createCertificateFromPemFormat(certificateString);
        } catch (CertificateException e) {
            throw new InvalidAuthenticationMeansException(provider, CLIENT_TRANSPORT_CERTIFICATE_NAME, "Cannot process certificate for thumbprint");
        }
    }
}
