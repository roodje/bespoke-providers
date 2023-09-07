package com.yolt.providers.direkt1822group.common;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.InvalidAuthenticationMeansException;
import com.yolt.providers.common.exception.MissingAuthenticationMeansException;
import com.yolt.providers.common.util.KeyUtil;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Data
@Builder(access = AccessLevel.PRIVATE)
public class Direkt1822GroupAuthenticationMeans {

    public static final String CLIENT_TRANSPORT_KEY_ID_NAME = "client-transport-private-keyid";
    public static final String CLIENT_TRANSPORT_CERTIFICATE_NAME = "client-transport-certificate";

    private final X509Certificate clientTransportCertificate;
    private final UUID transportKeyId;

    public static Direkt1822GroupAuthenticationMeans createAuthMeans(final Map<String, BasicAuthenticationMean> authenticationMeans, final String provider) {

        String transportCertificatePemFormat = getAuthenticationMeanValue(authenticationMeans, CLIENT_TRANSPORT_CERTIFICATE_NAME, provider);
        X509Certificate transportCertificate = createCertificate(transportCertificatePemFormat, CLIENT_TRANSPORT_CERTIFICATE_NAME, provider);

        return Direkt1822GroupAuthenticationMeans.builder()
                .transportKeyId(UUID.fromString(getAuthenticationMeanValue(authenticationMeans, CLIENT_TRANSPORT_KEY_ID_NAME, provider)))
                .clientTransportCertificate(transportCertificate)
                .build();
    }

    private static String getAuthenticationMeanValue(final Map<String, BasicAuthenticationMean> authenticationMeansMap,
                                                     final String key,
                                                     final String provider) {
        BasicAuthenticationMean authenticationMean = authenticationMeansMap.get(key);
        if (authenticationMean == null) {
            throw new MissingAuthenticationMeansException(provider, key);
        }
        return authenticationMean.getValue();
    }

    private static X509Certificate createCertificate(final String certificateString,
                                                     final String authenticationMeanName,
                                                     final String provider) {
        try {
            return KeyUtil.createCertificateFromPemFormat(certificateString);
        } catch (CertificateException e) {
            throw new InvalidAuthenticationMeansException(provider, authenticationMeanName,
                    "Cannot process certificate for thumbprint");
        }
    }

    public static Direkt1822GroupAuthenticationMeans createGroupAuthenticationMeans(final Map<String, BasicAuthenticationMean> authenticationMeans, final String provider) {
        return Direkt1822GroupAuthenticationMeans.builder()
                .transportKeyId(UUID.fromString(getAuthenticationMeanValue(authenticationMeans, CLIENT_TRANSPORT_KEY_ID_NAME, provider)))
                .clientTransportCertificate(createCertificate(getAuthenticationMeanValue(authenticationMeans, CLIENT_TRANSPORT_CERTIFICATE_NAME, provider), CLIENT_TRANSPORT_CERTIFICATE_NAME, provider))
                .build();
    }
}
