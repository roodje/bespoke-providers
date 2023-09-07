package com.yolt.providers.monorepogroup.raiffeisenatgroup.common.mapper.authmeans;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.InvalidAuthenticationMeansException;
import com.yolt.providers.common.exception.MissingAuthenticationMeansException;
import com.yolt.providers.common.util.KeyUtil;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.RaiffeisenAtGroupAuthenticationMeans;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.UUID;

import static com.yolt.providers.monorepogroup.raiffeisenatgroup.common.RaiffeisenAtGroupAuthenticationMeans.*;

public class DefaultRaiffeisenAtGroupAuthMeansMapper implements RaiffeisenAtGroupAuthMeansMapper {
    @Override
    public RaiffeisenAtGroupAuthenticationMeans map(final Map<String, BasicAuthenticationMean> authenticationMean, final String providerIdentifier) {
        X509Certificate transportCertificate = createTransportCertificate(providerIdentifier, authenticationMean);

        return new RaiffeisenAtGroupAuthenticationMeans(
                UUID.fromString(getAuthenticationMeanValueOrThrowException(authenticationMean, TRANSPORT_CERTIFICATE_ID_NAME, providerIdentifier)),
                transportCertificate,
                getAuthenticationMeanValueOrThrowException(authenticationMean, CLIENT_ID_NAME, providerIdentifier)
        );
    }

    @Override
    public RaiffeisenAtGroupAuthenticationMeans mapForAutoonboarding(Map<String, BasicAuthenticationMean> authenticationMean, String providerIdentifier) {
        X509Certificate transportCertificate = createTransportCertificate(providerIdentifier, authenticationMean);

        return new RaiffeisenAtGroupAuthenticationMeans(
                UUID.fromString(getAuthenticationMeanValueOrThrowException(authenticationMean, TRANSPORT_CERTIFICATE_ID_NAME, providerIdentifier)),
                transportCertificate,
                null
        );
    }

    private String getAuthenticationMeanValueOrThrowException(final Map<String, BasicAuthenticationMean> authenticationMeansMap,
                                                              final String key,
                                                              final String provider) {
        BasicAuthenticationMean authenticationMean = authenticationMeansMap.get(key);
        if (authenticationMean == null) {
            throw new MissingAuthenticationMeansException(provider, key);
        }
        return authenticationMean.getValue();
    }

    private X509Certificate createTransportCertificate(final String providerIdentifier,
                                                       final Map<String, BasicAuthenticationMean> authenticationMeans) {
        try {
            return KeyUtil.createCertificateFromPemFormat(
                    getAuthenticationMeanValueOrThrowException(authenticationMeans, TRANSPORT_CERTIFICATE_NAME, providerIdentifier));
        } catch (CertificateException e) {
            throw new InvalidAuthenticationMeansException(providerIdentifier, TRANSPORT_CERTIFICATE_NAME,
                    "Cannot process certificate for thumbprint");
        }
    }
}
