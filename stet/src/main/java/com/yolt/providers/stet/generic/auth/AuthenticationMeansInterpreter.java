package com.yolt.providers.stet.generic.auth;

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
public class AuthenticationMeansInterpreter {

    private final Map<String, BasicAuthenticationMean> basicAuthenticationMeans;
    private final String providerIdentifier;

    public String getNullableValue(String authMeanName) {
        if (!basicAuthenticationMeans.containsKey(authMeanName)) {
            return null;
        }
        return basicAuthenticationMeans.get(authMeanName).getValue();
    }

    public X509Certificate getCertificate(String authMeanName) {
        String authenticationValue = getValue(authMeanName);
        try {
            return KeyUtil.createCertificateFromPemFormat(authenticationValue);
        } catch (CertificateException e) {
            throw new InvalidAuthenticationMeansException(providerIdentifier, authMeanName, "Cannot process certificate from pem");
        }
    }

    public X509Certificate[] getCertificateChain(String authMeanName) {
        String authenticationValue = getValue(authMeanName);
        try {
            return KeyUtil.createCertificatesChainFromPemFormat(authenticationValue);
        } catch (CertificateException e) {
            throw new InvalidAuthenticationMeansException(providerIdentifier, authMeanName, "Cannot process certificate from pem");
        }
    }

    public UUID getUUID(String authMeanName) {
        return UUID.fromString(getValue(authMeanName));
    }

    public String getValue(String authMeanName) {
        if (!basicAuthenticationMeans.containsKey(authMeanName)) {
            throw new MissingAuthenticationMeansException(providerIdentifier, authMeanName);
        }
        return basicAuthenticationMeans.get(authMeanName).getValue();
    }
}
