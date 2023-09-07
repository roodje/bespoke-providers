package com.yolt.providers.sparkassenandlandesbanks.common;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.InvalidAuthenticationMeansException;
import com.yolt.providers.common.exception.MissingAuthenticationMeansException;
import com.yolt.providers.common.util.KeyUtil;
import com.yolt.securityutils.certificate.CertificateParser;
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
public class SparkassenAndLandesbanksAuthMeans {

    public static final String CLIENT_TRANSPORT_KEY_ID_NAME = "client-transport-private-keyid";
    public static final String CLIENT_TRANSPORT_CERTIFICATE_NAME = "client-transport-certificate";

    private final X509Certificate clientTransportCertificate;
    private final UUID transportKeyId;
    private final String clientId;

    public static SparkassenAndLandesbanksAuthMeans createAuthMeans(final Map<String, BasicAuthenticationMean> authenticationMeans, final String provider) {

        String transportCertificatePemFormat = getAuthenticationMeanValue(authenticationMeans, CLIENT_TRANSPORT_CERTIFICATE_NAME, provider);
        X509Certificate transportCertificate = createCertificate(transportCertificatePemFormat, CLIENT_TRANSPORT_CERTIFICATE_NAME, provider);

        return SparkassenAndLandesbanksAuthMeans.builder()
                .transportKeyId(UUID.fromString(getAuthenticationMeanValue(authenticationMeans, CLIENT_TRANSPORT_KEY_ID_NAME, provider)))
                .clientTransportCertificate(transportCertificate)
                .clientId(CertificateParser.getOrganizationIdentifier(transportCertificate))
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
}
