package com.yolt.providers.monorepogroup.bankvanbredagroup;

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
public class BankVanBredaGroupAuthenticationMeans {

    public static final String TRANSPORT_CERTIFICATE_NAME = "transport-certificate";
    public static final String TRANSPORT_KEY_ID = "transport-key-id";
    public static final String TPP_ID = "tpp-id";

    private final X509Certificate tlsCertificate;
    private final UUID transportKeyId;
    private final String tppId;

    public static BankVanBredaGroupAuthenticationMeans createAuthenticationMeans(final Map<String, BasicAuthenticationMean> authenticationMeans,
                                                                                 final String provider) {
        return BankVanBredaGroupAuthenticationMeans.builder()
                .transportKeyId(UUID.fromString(getAuthenticationMeanValue(authenticationMeans, TRANSPORT_KEY_ID, provider)))
                .tlsCertificate(createCertificate(
                        getAuthenticationMeanValue(authenticationMeans, TRANSPORT_CERTIFICATE_NAME, provider), TRANSPORT_CERTIFICATE_NAME, provider))
                .tppId(getAuthenticationMeanValue(authenticationMeans, TPP_ID, provider))
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