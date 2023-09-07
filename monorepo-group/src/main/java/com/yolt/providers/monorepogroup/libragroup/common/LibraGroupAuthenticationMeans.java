package com.yolt.providers.monorepogroup.libragroup.common;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.InvalidAuthenticationMeansException;
import com.yolt.providers.common.exception.MissingAuthenticationMeansException;
import com.yolt.providers.common.util.KeyUtil;
import lombok.*;
import org.bouncycastle.util.encoders.Hex;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.UUID;

@Data
@Builder(access = AccessLevel.PRIVATE)
public class LibraGroupAuthenticationMeans {

    public static final String SIGNING_CERTIFICATE_NAME = "signing-certificate";
    public static final String SIGNING_KEY_ID = "signing-key-id";
    public static final String CLIENT_ID = "client-id";
    public static final String CLIENT_SECRET = "client-secret";

    private final SigningData signingData;
    private final String clientId;
    private final String clientSecret;

    public static LibraGroupAuthenticationMeans createAuthenticationMeans(Map<String, BasicAuthenticationMean> authenticationMeans,
                                                                          String provider) {
        String signingCertificatePem = getAuthenticationMeanValue(authenticationMeans, SIGNING_CERTIFICATE_NAME, provider);
        X509Certificate signingCertificate = createSigningCertificate(
                signingCertificatePem, provider);
        UUID signingKeyId = UUID.fromString(getAuthenticationMeanValue(authenticationMeans, SIGNING_KEY_ID, provider));
        String rawCertificate = getRawCertificate(signingCertificatePem);
        String signingCertificateSerialNumber = "SN=" + Hex.toHexString(signingCertificate.getSerialNumber().toByteArray());
        return LibraGroupAuthenticationMeans.builder()
                .signingData(new SigningData(rawCertificate, signingCertificateSerialNumber, signingKeyId))
                .clientId(getAuthenticationMeanValue(authenticationMeans, CLIENT_ID, provider))
                .clientSecret(getAuthenticationMeanValue(authenticationMeans, CLIENT_SECRET, provider))
                .build();
    }

    private static String getRawCertificate(String signingCertificatePem) {
        return signingCertificatePem.replaceAll("-----BEGIN CERTIFICATE-----|-----END CERTIFICATE-----|\\s", "");
    }

    private static String getAuthenticationMeanValue(Map<String, BasicAuthenticationMean> authenticationMeansMap,
                                                     String key,
                                                     String provider) {
        BasicAuthenticationMean authenticationMean = authenticationMeansMap.get(key);
        if (authenticationMean == null) {
            throw new MissingAuthenticationMeansException(provider, key);
        }
        return authenticationMean.getValue();
    }

    private static X509Certificate createSigningCertificate(String certificateString,
                                                            String provider) {
        try {
            return KeyUtil.createCertificateFromPemFormat(certificateString);
        } catch (CertificateException e) {
            throw new InvalidAuthenticationMeansException(provider, SIGNING_CERTIFICATE_NAME,
                    "Cannot process certificate for thumbprint");
        }
    }

    @AllArgsConstructor
    @Getter
    public static class SigningData {
        private final String signingCertificateBase64;
        private final String signingCertificateSerialNumber;
        private final UUID signingKeyId;
    }
}
