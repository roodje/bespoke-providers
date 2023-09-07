package com.yolt.providers.raiffeisenbank.common.ais.auth;

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
public class RaiffeisenBankAuthenticationMeans {

    public static final String SIGNING_CERTIFICATE_NAME = "signing-certificate";
    public static final String TRANSPORT_CERTIFICATE_NAME = "transport-certificate";
    public static final String TRANSPORT_KEY_ID = "transport-key-id";
    public static final String CLIENT_ID = "client-Id";
    public static final String CLIENT_SECRET = "client-secret";
    public static final String SIGNING_KEY_ID = "signing-key-id";

    private final X509Certificate tlsCertificate;
    private final UUID transportKeyId;
    private final String clientId;
    private final String clientSecret;
    private final String signingCertificatePemFormat;
    private final UUID signingKeyId;

    public static RaiffeisenBankAuthenticationMeans createAuthenticationMeans(final Map<String, BasicAuthenticationMean> authenticationMeans, final String provider) {
        String signingCertificatePemFormat = getAuthenticationMeanValue(authenticationMeans, SIGNING_CERTIFICATE_NAME, provider);
        return RaiffeisenBankAuthenticationMeans.builder()
                .transportKeyId(UUID.fromString(getAuthenticationMeanValue(authenticationMeans, TRANSPORT_KEY_ID, provider)))
                .tlsCertificate(createCertificate(
                        getAuthenticationMeanValue(authenticationMeans, TRANSPORT_CERTIFICATE_NAME, provider), TRANSPORT_CERTIFICATE_NAME, provider))
                .clientId(getAuthenticationMeanValue(authenticationMeans, CLIENT_ID, provider))
                .clientSecret(getAuthenticationMeanValue(authenticationMeans, CLIENT_SECRET, provider))
                .signingKeyId(UUID.fromString(getAuthenticationMeanValue(authenticationMeans, SIGNING_KEY_ID, provider)))
                .signingCertificatePemFormat(signingCertificatePemFormat.replaceAll("[\\n\\r]+", ""))
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