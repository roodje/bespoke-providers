package com.yolt.providers.fabric.common.auth;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.InvalidAuthenticationMeansException;
import com.yolt.providers.common.exception.MissingAuthenticationMeansException;
import com.yolt.providers.common.util.KeyUtil;
import lombok.AllArgsConstructor;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;

@AllArgsConstructor
class FabricGroupAuthenticationMeansAdapter {

    private Map<String, BasicAuthenticationMean> authenticationMeans;
    private String providerIdentifier;

    String getNullableValue(final String authenticationKey) {
        if (!authenticationMeans.containsKey(authenticationKey)) {
            return null;
        }
        return authenticationMeans.get(authenticationKey).getValue();
    }

    X509Certificate getCertificate(final String authenticationKey) {
        String authenticationValue = getValue(authenticationKey);
        try {
            return KeyUtil.createCertificateFromPemFormat(authenticationValue);
        } catch (CertificateException e) {
            throw new InvalidAuthenticationMeansException(providerIdentifier, authenticationKey, "Cannot process certificate for thumbprint");
        }
    }

    String getValue(final String authenticationKey) {
        if (!authenticationMeans.containsKey(authenticationKey)) {
            throw new MissingAuthenticationMeansException(providerIdentifier, authenticationKey);
        }
        return authenticationMeans.get(authenticationKey).getValue();
    }
}