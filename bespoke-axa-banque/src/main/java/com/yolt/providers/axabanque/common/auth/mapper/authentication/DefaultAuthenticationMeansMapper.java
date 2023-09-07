package com.yolt.providers.axabanque.common.auth.mapper.authentication;

import com.yolt.providers.axabanque.common.auth.GroupAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.InvalidAuthenticationMeansException;
import com.yolt.providers.common.exception.MissingAuthenticationMeansException;
import com.yolt.providers.common.util.KeyUtil;
import com.yolt.securityutils.certificate.CertificateParser;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.UUID;

import static com.yolt.providers.axabanque.common.auth.GroupAuthenticationMeans.TRANSPORT_CERTIFICATE;
import static com.yolt.providers.axabanque.common.auth.GroupAuthenticationMeans.TRANSPORT_KEY_ID;

public class DefaultAuthenticationMeansMapper implements AuthenticationMeansMapper {

    @Override
    public GroupAuthenticationMeans map(Map<String, BasicAuthenticationMean> authenticationMeans, String providerIdentifier) {
        X509Certificate transportCertificate = createTransportCertificate(providerIdentifier, authenticationMeans);
        return new GroupAuthenticationMeans(
                UUID.fromString(getAuthenticationMeanValueOrThrowException(authenticationMeans, TRANSPORT_KEY_ID, providerIdentifier)),
                transportCertificate,
                CertificateParser.getOrganizationIdentifier(transportCertificate));
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
                    getAuthenticationMeanValueOrThrowException(authenticationMeans, TRANSPORT_CERTIFICATE, providerIdentifier));
        } catch (CertificateException e) {
            throw new InvalidAuthenticationMeansException(providerIdentifier, TRANSPORT_CERTIFICATE,
                    "Cannot process certificate for thumbprint");
        }
    }
}
