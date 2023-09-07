package com.yolt.providers.knabgroup.common.auth;

import com.yolt.providers.common.cryptography.Signer;
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
public class KnabGroupAuthenticationMeans {

    public static final String SIGNING_CERTIFICATE_NAME = "signing-certificate";
    public static final String TRANSPORT_CERTIFICATE_NAME = "transport-certificate";
    public static final String TRANSPORT_KEY_ID = "transport-private-key-id";
    public static final String SIGNING_KEY_ID = "signing-private-key-id";
    public static final String CLIENT_ID = "client-id";
    public static final String CLIENT_SECRET = "client-secret";

    private final X509Certificate tlsCertificate;
    private final X509Certificate signingCertificate;
    private final String signingCertificateInBase64;
    private final UUID transportKeyId;
    private final UUID signingKeyId;
    private final String clientId;
    private final String clientSecret;

    public SignatureData getSigningData(Signer signer) {
        return SignatureData.builder()
                .signer(signer)
                .signingKeyId(signingKeyId)
                .signingCertificate(signingCertificate)
                .signingCertificateInBase64(signingCertificateInBase64)
                .build();
    }

    public static KnabGroupAuthenticationMeans createKnabGroupAuthenticationMeans(final Map<String, BasicAuthenticationMean> authenticationMeans, final String provider) {
        String signingCertificatePemFormat = getAuthenticationMeanValue(authenticationMeans, SIGNING_CERTIFICATE_NAME, provider);
        X509Certificate signingCertificate = createCertificate(signingCertificatePemFormat, SIGNING_CERTIFICATE_NAME, provider);
        return KnabGroupAuthenticationMeans.builder()
                .transportKeyId(UUID.fromString(getAuthenticationMeanValue(authenticationMeans, TRANSPORT_KEY_ID, provider)))
                .signingKeyId(UUID.fromString(getAuthenticationMeanValue(authenticationMeans, SIGNING_KEY_ID, provider)))
                .signingCertificate(signingCertificate)
                .tlsCertificate(createCertificate(
                        getAuthenticationMeanValue(authenticationMeans, TRANSPORT_CERTIFICATE_NAME, provider), TRANSPORT_CERTIFICATE_NAME, provider))
                .clientId(getAuthenticationMeanValue(authenticationMeans, CLIENT_ID, provider))
                .clientSecret(getAuthenticationMeanValue(authenticationMeans, CLIENT_SECRET, provider))
                .signingCertificateInBase64(stripKeyFromBeginEndTagsAndNewLines(signingCertificatePemFormat))
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

    private static String stripKeyFromBeginEndTagsAndNewLines(final String key) {
        return KeyUtil.stripKeyFromBeginEndTags(key, "CERTIFICATE").replaceAll("[\\n\\r]+", "");
    }
}