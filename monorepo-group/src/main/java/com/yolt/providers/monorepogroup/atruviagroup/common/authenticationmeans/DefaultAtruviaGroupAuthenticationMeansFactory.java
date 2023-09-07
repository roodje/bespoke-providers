package com.yolt.providers.monorepogroup.atruviagroup.common.authenticationmeans;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.InvalidAuthenticationMeansException;
import com.yolt.providers.common.exception.MissingAuthenticationMeansException;
import com.yolt.providers.common.util.KeyUtil;
import lombok.RequiredArgsConstructor;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
public class DefaultAtruviaGroupAuthenticationMeansFactory implements AtruviaGroupAuthenticationMeansFactory {

    private final String providerIdentifier;

    @Override
    public AtruviaGroupAuthenticationMeans toAuthenticationMeans(Map<String, BasicAuthenticationMean> authenticationMeanMap) {
        var certificate = getCertificate(authenticationMeanMap);
        return AtruviaGroupAuthenticationMeans
                .builder()
                .clientCertificate(certificate)
                .clientCertificateKey(UUID.fromString(getValue(authenticationMeanMap, CLIENT_SIGNING_KEY_ID_NAME)))
                .build();
    }

    X509Certificate getCertificate(Map<String, BasicAuthenticationMean> authenticationMeans) {
        String authenticationValue = getValue(authenticationMeans, CLIENT_SIGNING_CERTIFICATE_NAME);
        try {
            return KeyUtil.createCertificateFromPemFormat(authenticationValue);
        } catch (CertificateException e) {
            throw new InvalidAuthenticationMeansException(providerIdentifier, CLIENT_SIGNING_CERTIFICATE_NAME, "Cannot process certificate for thumbprint");
        }
    }

    String getValue(Map<String, BasicAuthenticationMean> authenticationMeans, String authenticationKey) {
        if (!authenticationMeans.containsKey(authenticationKey)) {
            throw new MissingAuthenticationMeansException(providerIdentifier, authenticationKey);
        }
        return authenticationMeans.get(authenticationKey).getValue();
    }
}
