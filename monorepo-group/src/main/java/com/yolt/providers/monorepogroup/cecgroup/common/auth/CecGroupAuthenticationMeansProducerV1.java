package com.yolt.providers.monorepogroup.cecgroup.common.auth;

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
public class CecGroupAuthenticationMeansProducerV1 implements CecGroupAuthenticationMeansProducer {

    public static final String TRANSPORT_CERTIFICATE_NAME = "transport-certificate";
    public static final String TRANSPORT_KEY_ID_NAME = "transport-key-id";
    public static final String SIGNING_CERTIFICATE_NAME = "signing-certificate";
    public static final String SIGNING_KEY_ID_NAME = "signing-key-id";
    public static final String CLIENT_ID_NAME = "client-id";
    public static final String CLIENT_SECRET_NAME = "client-secret";

    @Override
    public Optional<KeyRequirements> getTransportKeyRequirements() {
        return HsmEidasUtils.getKeyRequirements(TRANSPORT_KEY_ID_NAME, TRANSPORT_CERTIFICATE_NAME);
    }

    @Override
    public Optional<KeyRequirements> getSigningKeyRequirements() {
        return HsmEidasUtils.getKeyRequirements(SIGNING_KEY_ID_NAME, SIGNING_CERTIFICATE_NAME);
    }

    @Override
    public Map<String, TypedAuthenticationMeans> getTypedAuthenticationMeans() {
        Map<String, TypedAuthenticationMeans> typedAuthMeans = new HashMap<>();
        typedAuthMeans.put(TRANSPORT_KEY_ID_NAME, TypedAuthenticationMeans.KEY_ID);
        typedAuthMeans.put(TRANSPORT_CERTIFICATE_NAME, TypedAuthenticationMeans.CERTIFICATE_PEM);
        typedAuthMeans.put(SIGNING_KEY_ID_NAME, TypedAuthenticationMeans.KEY_ID);
        typedAuthMeans.put(SIGNING_CERTIFICATE_NAME, TypedAuthenticationMeans.CERTIFICATE_PEM);
        typedAuthMeans.put(CLIENT_ID_NAME, TypedAuthenticationMeans.CLIENT_ID_STRING);
        typedAuthMeans.put(CLIENT_SECRET_NAME, TypedAuthenticationMeans.CLIENT_SECRET_STRING);
        return typedAuthMeans;
    }

    @Override
    public CecGroupAuthenticationMeans createAuthenticationMeans(Map<String, BasicAuthenticationMean> authMeans,
                                                                 String providerIdentifier) {
        return CecGroupAuthenticationMeans.builder()
                .transportCertificate(getCertificate(authMeans, providerIdentifier, TRANSPORT_CERTIFICATE_NAME))
                .transportKeyId(getValue(authMeans, providerIdentifier, TRANSPORT_KEY_ID_NAME))
                .signingCertificate(getCertificate(authMeans, providerIdentifier, SIGNING_CERTIFICATE_NAME))
                .signingKeyId(getValue(authMeans, providerIdentifier, SIGNING_KEY_ID_NAME))
                .clientId(getValue(authMeans, providerIdentifier, CLIENT_ID_NAME))
                .clientSecret(getValue(authMeans, providerIdentifier, CLIENT_SECRET_NAME))
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
