package com.yolt.providers.monorepogroup.olbgroup.common.auth;

import com.yolt.providers.common.cryptography.HsmEidasUtils;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.exception.InvalidAuthenticationMeansException;
import com.yolt.providers.common.exception.MissingAuthenticationMeansException;
import com.yolt.providers.common.util.KeyUtil;
import lombok.RequiredArgsConstructor;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class OlbGroupAuthenticationMeansProducerV1 implements OlbGroupAuthenticationMeansProducer {

    public static final String TRANSPORT_CERTIFICATE_NAME = "transport-certificate";
    public static final String TRANSPORT_KEY_ID_NAME = "transport-key-id";

    @Override
    public Optional<KeyRequirements> getTransportKeyRequirements() {
        return HsmEidasUtils.getKeyRequirements(TRANSPORT_KEY_ID_NAME, TRANSPORT_CERTIFICATE_NAME);
    }

    @Override
    public Map<String, TypedAuthenticationMeans> getTypedAuthenticationMeans() {
        Map<String, TypedAuthenticationMeans> typedAuthMeans = new HashMap<>();
        typedAuthMeans.put(TRANSPORT_KEY_ID_NAME, TypedAuthenticationMeans.KEY_ID);
        typedAuthMeans.put(TRANSPORT_CERTIFICATE_NAME, TypedAuthenticationMeans.CERTIFICATE_PEM);
        return typedAuthMeans;
    }

    @Override
    public OlbGroupAuthenticationMeans createAuthenticationMeans(Map<String, BasicAuthenticationMean> authMeans,
                                                                 String providerIdentifier) {
        return OlbGroupAuthenticationMeans.builder()
                .transportCertificate(getCertificate(authMeans, providerIdentifier, TRANSPORT_CERTIFICATE_NAME))
                .transportKeyId(getValue(authMeans, providerIdentifier, TRANSPORT_KEY_ID_NAME))
                .build();
    }

    private X509Certificate getCertificate(Map<String, BasicAuthenticationMean> authMeans,
                                           String providerIdentifier,
                                           String authKey) {
        String authValue = getValue(authMeans, providerIdentifier, authKey);
        try {
            return KeyUtil.createCertificateFromPemFormat(authValue);
        } catch (CertificateException e) {
            throw new InvalidAuthenticationMeansException(providerIdentifier, authKey, "Cannot process certificate for thumbprint");
        }
    }

    private String getValue(Map<String, BasicAuthenticationMean> authMeans,
                            String providerIdentifier,
                            String authKey) {
        var authValue = authMeans.get(authKey);
        if (authValue == null) {
            throw new MissingAuthenticationMeansException(providerIdentifier, authKey);
        }
        return authValue.getValue();
    }
}
