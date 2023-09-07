package com.yolt.providers.fineco.auth;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.InvalidAuthenticationMeansException;
import com.yolt.providers.common.util.KeyUtil;
import lombok.Builder;
import lombok.Data;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;

@Data
@Builder
public class FinecoAuthenticationMeans {

    public static final String CLIENT_ID_NAME = "client-id";
    public static final String CLIENT_TRANSPORT_KEY_ID_NAME = "client-transport-private-keyid";
    public static final String CLIENT_TRANSPORT_CERTIFICATE_NAME = "client-transport-certificate";

    private final String clientId;
    private final String clientCertificateKey;
    private final X509Certificate clientCertificate;

    public static FinecoAuthenticationMeans fromAuthenticationMeans(final Map<String, BasicAuthenticationMean> typedAuthenticationMeans,
                                                                    final String provider) {
        String clientId = typedAuthenticationMeans.get(CLIENT_ID_NAME).getValue();
        String keyId = typedAuthenticationMeans.get(CLIENT_TRANSPORT_KEY_ID_NAME).getValue();
        X509Certificate clientTransportCertificate = createCertificate(
                typedAuthenticationMeans.get(CLIENT_TRANSPORT_CERTIFICATE_NAME).getValue(), provider);

        return FinecoAuthenticationMeans.builder()
                .clientId(clientId)
                .clientCertificateKey(keyId)
                .clientCertificate(clientTransportCertificate)
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