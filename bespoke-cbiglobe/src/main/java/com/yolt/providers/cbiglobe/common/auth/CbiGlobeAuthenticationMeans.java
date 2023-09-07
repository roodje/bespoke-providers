package com.yolt.providers.cbiglobe.common.auth;

import com.yolt.providers.cbiglobe.common.model.SignatureData;
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
public class CbiGlobeAuthenticationMeans {

    public static final String TRANSPORT_CERTIFICATE_NAME = "transport-certificate";
    public static final String TRANSPORT_KEY_ID_NAME = "transport-key-id";
    public static final String SIGNING_CERTIFICATE_NAME = "signing-certificate";
    public static final String SIGNING_KEY_ID_NAME = "signing-key-id";
    public static final String CLIENT_ID_STRING_NAME = "client-id";
    public static final String CLIENT_SECRET_STRING_NAME = "client-secret";

    private final X509Certificate transportCertificate;
    private final String transportKeyId;
    private final X509Certificate signingCertificate;
    private final String signingKeyId;
    private final String clientId;
    private final String clientSecret;

    public SignatureData getSigningData(Signer signer) {
        return SignatureData.builder()
                .signer(signer)
                .signingKeyId(UUID.fromString(signingKeyId))
                .signingCertificate(signingCertificate)
                .build();
    }

    public static CbiGlobeAuthenticationMeans getCbiGlobeAuthenticationMeans(final Map<String, BasicAuthenticationMean> authenticationMeans, final String providerKey) {
        X509Certificate transportCertificate = createCertificate(getAuthenticationMeanValue(authenticationMeans, TRANSPORT_CERTIFICATE_NAME, providerKey), providerKey);
        String transportKeyId = getAuthenticationMeanValue(authenticationMeans, TRANSPORT_KEY_ID_NAME, providerKey);
        X509Certificate signingCertificate = createCertificate(getAuthenticationMeanValue(authenticationMeans, SIGNING_CERTIFICATE_NAME, providerKey), providerKey);
        String signingKeyId = getAuthenticationMeanValue(authenticationMeans, SIGNING_KEY_ID_NAME, providerKey);
        String clientId = getAuthenticationMeanValue(authenticationMeans, CLIENT_ID_STRING_NAME, providerKey);
        String clientSecret = getAuthenticationMeanValue(authenticationMeans, CLIENT_SECRET_STRING_NAME, providerKey);

        return CbiGlobeAuthenticationMeans.builder()
                .transportCertificate(transportCertificate)
                .transportKeyId(transportKeyId)
                .signingCertificate(signingCertificate)
                .signingKeyId(signingKeyId)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .build();
    }

    private static String getAuthenticationMeanValue(final Map<String, BasicAuthenticationMean> authenticationMeansMap,
                                                     final String key,
                                                     final String providerKey) {
        BasicAuthenticationMean authenticationMean = authenticationMeansMap.get(key);
        if (authenticationMean == null) {
            throw new MissingAuthenticationMeansException(providerKey, key);
        }
        return authenticationMean.getValue();
    }

    private static X509Certificate createCertificate(final String certificateString,
                                                     final String providerKey) {
        try {
            return KeyUtil.createCertificateFromPemFormat(certificateString);
        } catch (CertificateException e) {
            throw new InvalidAuthenticationMeansException(providerKey, TRANSPORT_CERTIFICATE_NAME, "Cannot process certificate for thumbprint");
        }
    }
}
