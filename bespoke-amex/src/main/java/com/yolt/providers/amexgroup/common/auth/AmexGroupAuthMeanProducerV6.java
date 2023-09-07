package com.yolt.providers.amexgroup.common.auth;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.InvalidAuthenticationMeansException;
import com.yolt.providers.common.exception.MissingAuthenticationMeansException;
import com.yolt.providers.common.util.KeyUtil;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.UUID;

import static com.yolt.providers.amexgroup.common.utils.AmexAuthMeansFields.*;

public class AmexGroupAuthMeanProducerV6 implements AmexGroupAuthMeanProducer {
    @Override
    public AmexGroupAuthMeans createAuthMeans(Map<String, BasicAuthenticationMean> authenticationMeans, String provider) {

        String transportCertificatePemFormat = getAuthenticationMeanValue(authenticationMeans, CLIENT_TRANSPORT_CERTIFICATE_ROTATION, provider);
        X509Certificate transportCertificate = createCertificate(transportCertificatePemFormat, provider);

        return new AmexGroupAuthMeansV6(
                getAuthenticationMeanValue(authenticationMeans, CLIENT_ID, provider),
                getAuthenticationMeanValue(authenticationMeans, CLIENT_SECRET, provider),
                transportCertificate,
                UUID.fromString(getAuthenticationMeanValue(authenticationMeans, CLIENT_TRANSPORT_KEY_ID_ROTATION, provider)));
    }

    private String getAuthenticationMeanValue(final Map<String, BasicAuthenticationMean> authenticationMeansMap,
                                                     final String key,
                                                     final String provider) {
        BasicAuthenticationMean authenticationMean = authenticationMeansMap.get(key);
        if (authenticationMean == null) {
            throw new MissingAuthenticationMeansException(provider, key);
        }
        return authenticationMean.getValue();
    }

    private X509Certificate createCertificate(final String certificateString,
                                              final String provider) {
        try {
            return KeyUtil.createCertificateFromPemFormat(certificateString);
        } catch (CertificateException e) {
            throw new InvalidAuthenticationMeansException(provider, CLIENT_TRANSPORT_CERTIFICATE_ROTATION,
                    "Cannot process certificate for thumbprint");
        }
    }
}
