package com.yolt.providers.brdgroup.common;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.InvalidAuthenticationMeansException;
import com.yolt.providers.common.util.KeyUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class BrdGroupAuthenticationMeans {

    public static final String TRANSPORT_CERTIFICATE_NAME = "transport-certificate";
    public static final String TRANSPORT_KEY_ID_NAME = "transport-key-id";

    private final UUID transportKeyId;
    private final X509Certificate transportCertificate;

    public static BrdGroupAuthenticationMeans createAuthenticationMeans(Map<String, BasicAuthenticationMean> authenticationMeans,
                                                                        String provider) {
        return new BrdGroupAuthenticationMeans(
                UUID.fromString(authenticationMeans.get(TRANSPORT_KEY_ID_NAME).getValue()),
                createCertificate(authenticationMeans.get(TRANSPORT_CERTIFICATE_NAME).getValue(), TRANSPORT_CERTIFICATE_NAME, provider)
        );
    }

    private static X509Certificate createCertificate(String certificate, String authMeanName, String provider) {
        try {
            return KeyUtil.createCertificateFromPemFormat(certificate);
        } catch (CertificateException e) {
            throw new InvalidAuthenticationMeansException(provider, authMeanName, "Cannot process certificate for thumbprint");
        }
    }
}
