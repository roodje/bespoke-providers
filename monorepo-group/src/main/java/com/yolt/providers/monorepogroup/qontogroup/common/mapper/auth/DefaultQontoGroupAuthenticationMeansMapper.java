package com.yolt.providers.monorepogroup.qontogroup.common.mapper.auth;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.InvalidAuthenticationMeansException;
import com.yolt.providers.common.exception.MissingAuthenticationMeansException;
import com.yolt.providers.common.util.KeyUtil;
import com.yolt.providers.monorepogroup.qontogroup.common.QontoGroupAuthenticationMeans;
import com.yolt.providers.monorepogroup.qontogroup.common.util.Fingerprint;
import lombok.RequiredArgsConstructor;

import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.UUID;

import static com.yolt.providers.monorepogroup.qontogroup.common.QontoGroupAuthenticationMeans.*;

@RequiredArgsConstructor
public class DefaultQontoGroupAuthenticationMeansMapper implements QontoGroupAuthenticationMeansMapper {

    private static final String PEM_FORMAT_EXTENSION = ".pem";
    private final String s3baseUrl;

    @Override
    public QontoGroupAuthenticationMeans map(Map<String, BasicAuthenticationMean> authenticationMeans, String providerIdentifier) {
        String signingCertificatePem = getAuthenticationMeanValue(authenticationMeans, SIGNING_CERTIFICATE_NAME, providerIdentifier);
        X509Certificate signingCertificate = createSigningCertificate(
                signingCertificatePem, providerIdentifier);
        UUID signingKeyId = UUID.fromString(getAuthenticationMeanValue(authenticationMeans, SIGNING_CERTIFICATE_ID_NAME, providerIdentifier));
        String certificateUrl = getCertificateUrl(s3baseUrl, signingCertificate, providerIdentifier);
        return new QontoGroupAuthenticationMeans(
                new QontoGroupAuthenticationMeans.SigningData(certificateUrl, signingKeyId),
                getAuthenticationMeanValue(authenticationMeans, CLIENT_ID_NAME, providerIdentifier),
                getAuthenticationMeanValue(authenticationMeans, CLIENT_SECRET_NAME, providerIdentifier));
    }

    private String getCertificateUrl(final String s3baseUrl, final X509Certificate signingCertificate, String provider) {
        final String fingerprint;
        try {
            fingerprint = new Fingerprint(signingCertificate.getEncoded()).toString();
            return s3baseUrl + "/" + fingerprint + PEM_FORMAT_EXTENSION;
        } catch (CertificateEncodingException e) {
            throw new InvalidAuthenticationMeansException(provider, SIGNING_CERTIFICATE_NAME, "Failed to create fingerprint from certificate");
        }
    }

    private String getAuthenticationMeanValue(Map<String, BasicAuthenticationMean> authenticationMeansMap,
                                              String key,
                                              String provider) {
        BasicAuthenticationMean authenticationMean = authenticationMeansMap.get(key);
        if (authenticationMean == null) {
            throw new MissingAuthenticationMeansException(provider, key);
        }
        return authenticationMean.getValue();
    }

    private X509Certificate createSigningCertificate(String certificateString,
                                                     String provider) {
        try {
            return KeyUtil.createCertificateFromPemFormat(certificateString);
        } catch (CertificateException e) {
            throw new InvalidAuthenticationMeansException(provider, SIGNING_CERTIFICATE_NAME,
                    "Cannot process certificate for thumbprint");
        }
    }
}
