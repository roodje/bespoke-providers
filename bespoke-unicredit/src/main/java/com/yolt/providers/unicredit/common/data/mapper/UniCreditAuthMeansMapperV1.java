package com.yolt.providers.unicredit.common.data.mapper;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.InvalidAuthenticationMeansException;
import com.yolt.providers.common.util.KeyUtil;
import com.yolt.providers.unicredit.common.auth.UniCreditAuthMeans;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;

import static com.yolt.providers.unicredit.common.auth.UniCreditAuthMeans.*;


public class UniCreditAuthMeansMapperV1 implements UniCreditAuthMeansMapper {

    @Override
    public UniCreditAuthMeans fromBasicAuthenticationMeans(Map<String, BasicAuthenticationMean> basicAuthenticationMeans, String provider) {
        return UniCreditAuthMeans.builder()
                .eidasCertificate(createCertificate(basicAuthenticationMeans.get(EIDAS_CERTIFICATE).getValue(), provider))
                .eidasKeyId(basicAuthenticationMeans.get(EIDAS_KEY_ID).getValue())
                .clientEmail(basicAuthenticationMeans.get(CLIENT_EMAIL).getValue())
                .build();
    }

    private X509Certificate createCertificate(final String certificateString, final String provider) {
        try {
            return KeyUtil.createCertificateFromPemFormat(certificateString);
        } catch (CertificateException e) {
            throw new InvalidAuthenticationMeansException(provider, EIDAS_CERTIFICATE, "Cannot process certificate for thumbprint");
        }
    }
}
