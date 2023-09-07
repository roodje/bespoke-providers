package com.yolt.providers.monorepogroup.chebancagroup.common.mapper.authmeans;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.InvalidAuthenticationMeansException;
import com.yolt.providers.common.exception.MissingAuthenticationMeansException;
import com.yolt.providers.common.util.KeyUtil;
import com.yolt.providers.monorepogroup.chebancagroup.common.auth.CheBancaGroupAuthenticationMeans;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.UUID;

import static com.yolt.providers.monorepogroup.chebancagroup.common.auth.CheBancaGroupAuthenticationMeans.*;

public class DefaultCheBancaGroupAuthMeansMapper implements CheBancaGroupAuthMeansMapper {
    @Override
    public CheBancaGroupAuthenticationMeans map(final Map<String, BasicAuthenticationMean> authenticationMean, final String providerIdentifier) {
        X509Certificate transportCertificate = createTransportCertificate(providerIdentifier, TRANSPORT_CERTIFICATE_NAME, authenticationMean);
        X509Certificate signingCertificate = createTransportCertificate(providerIdentifier, SIGNING_CERTIFICATE_NAME, authenticationMean);

        return new CheBancaGroupAuthenticationMeans(
                UUID.fromString(getAuthenticationMeanValueOrThrowException(authenticationMean, TRANSPORT_CERTIFICATE_ID_NAME, providerIdentifier)),
                transportCertificate,
                UUID.fromString(getAuthenticationMeanValueOrThrowException(authenticationMean, SIGNING_CERTIFICATE_ID_NAME, providerIdentifier)),
                signingCertificate,
                getAuthenticationMeanValueOrThrowException(authenticationMean, CLIENT_ID_NAME, providerIdentifier),
                getAuthenticationMeanValueOrThrowException(authenticationMean, CLIENT_SECRET_NAME, providerIdentifier),
                getAuthenticationMeanValueOrThrowException(authenticationMean, CLIENT_APP_ID, providerIdentifier)
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
                                                       final String certificateType,
                                                       final Map<String, BasicAuthenticationMean> authenticationMeans) {
        try {
            return KeyUtil.createCertificateFromPemFormat(
                    getAuthenticationMeanValueOrThrowException(authenticationMeans, certificateType, providerIdentifier));
        } catch (CertificateException e) {
            throw new InvalidAuthenticationMeansException(providerIdentifier, certificateType,
                    "Cannot process certificate for thumbprint");
        }
    }
}
