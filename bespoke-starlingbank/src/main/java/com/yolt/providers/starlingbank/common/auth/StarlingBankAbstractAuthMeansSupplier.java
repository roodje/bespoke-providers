package com.yolt.providers.starlingbank.common.auth;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.InvalidAuthenticationMeansException;
import com.yolt.providers.common.exception.MissingAuthenticationMeansException;
import com.yolt.providers.common.util.KeyUtil;
import org.apache.commons.lang3.StringUtils;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;

public abstract class StarlingBankAbstractAuthMeansSupplier {

    protected String getValue(Map<String, BasicAuthenticationMean> authMeans, String providerIdentifier, String authKey) {
        String authValue = getNullableValue(authMeans, authKey);
        if (authValue == null) {
            throw new MissingAuthenticationMeansException(providerIdentifier, authKey);
        }
        return authValue;
    }

    protected String getNullableValue(Map<String, BasicAuthenticationMean> authMeans, String authKey) {
        if (authMeans.containsKey(authKey)) {
            return authMeans.get(authKey).getValue();
        }
        return null;
    }

    protected X509Certificate createTransportCertificate(final String providerIdentifier,
                                                         final Map<String, BasicAuthenticationMean> authMeans,
                                                         final String transportCertificateName) {
        try {
            String certificate = getNullableValue(authMeans, transportCertificateName);
            return StringUtils.isNotBlank(certificate) ? KeyUtil.createCertificateFromPemFormat(certificate) : null;
        } catch (CertificateException e) {
            throw new InvalidAuthenticationMeansException(providerIdentifier, transportCertificateName,
                    "Cannot process certificate for thumbprint");
        }
    }
}
