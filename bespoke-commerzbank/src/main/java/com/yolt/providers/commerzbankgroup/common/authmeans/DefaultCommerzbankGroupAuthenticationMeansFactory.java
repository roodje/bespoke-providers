package com.yolt.providers.commerzbankgroup.common.authmeans;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.InvalidAuthenticationMeansException;
import com.yolt.providers.common.exception.MissingAuthenticationMeansException;
import com.yolt.providers.common.util.KeyUtil;
import com.yolt.securityutils.certificate.CertificateParser;
import lombok.RequiredArgsConstructor;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
public class DefaultCommerzbankGroupAuthenticationMeansFactory implements CommerzbankGroupAuthenticationMeansFactory {

    private final String providerIdentifier;

    @Override
    public CommerzbankGroupAuthenticationMeans toAuthenticationMeans(Map<String, BasicAuthenticationMean> authenticationMeanMap) {
        var certificate = getCertificate(authenticationMeanMap);
        return new DefaultCommerzbankgroupAuthenticationMeans
                .DefaultCommerzbankgroupAuthenticationMeansBuilder()
                .clientCertificate(certificate)
                .clientCertificateKey(UUID.fromString(getValue(authenticationMeanMap, CLIENT_TRANSPORT_KEY_ID_NAME)))
                .organizationIdentifier(CertificateParser.getOrganizationIdentifier(certificate))
                .build();
    }

    X509Certificate getCertificate(Map<String, BasicAuthenticationMean> authenticationMeans) {
        String authenticationValue = getValue(authenticationMeans, CommerzbankGroupAuthenticationMeansFactory.CLIENT_TRANSPORT_CERTIFICATE_NAME);
        try {
            return KeyUtil.createCertificateFromPemFormat(authenticationValue);
        } catch (CertificateException e) {
            throw new InvalidAuthenticationMeansException(providerIdentifier, CommerzbankGroupAuthenticationMeansFactory.CLIENT_TRANSPORT_CERTIFICATE_NAME, "Cannot process certificate for thumbprint");
        }
    }

    String getValue(Map<String, BasicAuthenticationMean> authenticationMeans, String authenticationKey) {
        if (!authenticationMeans.containsKey(authenticationKey)) {
            throw new MissingAuthenticationMeansException(providerIdentifier, authenticationKey);
        }
        return authenticationMeans.get(authenticationKey).getValue();
    }
}
