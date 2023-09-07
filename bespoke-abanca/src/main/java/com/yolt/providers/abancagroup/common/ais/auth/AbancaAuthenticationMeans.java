package com.yolt.providers.abancagroup.common.ais.auth;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.InvalidAuthenticationMeansException;
import com.yolt.providers.common.exception.MissingAuthenticationMeansException;
import com.yolt.providers.common.util.KeyUtil;
import lombok.Builder;
import lombok.Data;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class AbancaAuthenticationMeans {

    public static final String SIGNING_CERTIFICATE_NAME = "signing-certificate";
    public static final String TRANSPORT_CERTIFICATE_NAME = "transport-certificate";
    public static final String SIGNING_KEY_ID = "signing-key-id";
    public static final String TRANSPORT_KEY_ID = "transport-key-id";
    public static final String CLIENT_ID = "client-Id";
    public static final String API_KEY = "api-key";

    private final X509Certificate tlsCertificate;
    private final UUID transportKeyId;
    private final UUID clientId;
    private final UUID apiKey;
    private final String signingCertificateSerialNumber;
    private final UUID signingKeyId;

    public static AbancaAuthenticationMeans createAuthenticationMeans(final Map<String, BasicAuthenticationMean> authenticationMeans, final String provider) {
        String signingCertificatePemFormat = getAuthenticationMeanValue(authenticationMeans, SIGNING_CERTIFICATE_NAME, provider);
        X509Certificate signingCertificate = createCertificate(signingCertificatePemFormat, SIGNING_CERTIFICATE_NAME, provider);
        return AbancaAuthenticationMeans.builder()
                .transportKeyId(UUID.fromString(getAuthenticationMeanValue(authenticationMeans, TRANSPORT_KEY_ID, provider)))
                .tlsCertificate(createCertificate(
                        getAuthenticationMeanValue(authenticationMeans, TRANSPORT_CERTIFICATE_NAME, provider), TRANSPORT_CERTIFICATE_NAME, provider))
                .signingKeyId(UUID.fromString(getAuthenticationMeanValue(authenticationMeans, SIGNING_KEY_ID, provider)))
                .clientId(UUID.fromString(getAuthenticationMeanValue(authenticationMeans, CLIENT_ID, provider)))
                .apiKey(UUID.fromString(getAuthenticationMeanValue(authenticationMeans, API_KEY, provider)))
                .signingCertificateSerialNumber(generateSigningCertificateSerialNumber(signingCertificate))
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

    private static String generateSigningCertificateSerialNumber(final X509Certificate signingCertificate) {
        return "SN=" + signingCertificate.getSerialNumber().toString(16).toUpperCase();
    }
}