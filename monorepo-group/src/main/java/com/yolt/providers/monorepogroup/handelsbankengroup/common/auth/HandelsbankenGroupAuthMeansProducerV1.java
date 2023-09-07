package com.yolt.providers.monorepogroup.handelsbankengroup.common.auth;

import com.yolt.providers.common.cryptography.HsmEidasUtils;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.RenderingType;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.authenticationmeans.types.StringType;
import com.yolt.providers.common.exception.InvalidAuthenticationMeansException;
import com.yolt.providers.common.exception.MissingAuthenticationMeansException;
import com.yolt.providers.common.util.KeyUtil;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class HandelsbankenGroupAuthMeansProducerV1 implements HandelsbankenGroupAuthMeansProducer {

    public static final String TRANSPORT_CERTIFICATE_NAME = "transport-certificate";
    public static final String TRANSPORT_KEY_ID_NAME = "transport-key-id";
    public static final String TPP_ID_NAME = "tpp-id";
    public static final String CLIENT_ID_NAME = "client-id";
    public static final String APPLICATION_NAME_NAME = "app-name";
    public static final String APPLICATION_DESCRIPTION_NAME = "app-description";

    @Override
    public Map<String, TypedAuthenticationMeans> getTypedAuthenticationMeans() {
        Map<String, TypedAuthenticationMeans> typedAuthMeans = new HashMap<>();
        typedAuthMeans.put(TRANSPORT_KEY_ID_NAME, TypedAuthenticationMeans.KEY_ID);
        typedAuthMeans.put(TRANSPORT_CERTIFICATE_NAME, TypedAuthenticationMeans.CERTIFICATE_PEM);
        typedAuthMeans.put(TPP_ID_NAME, TypedAuthenticationMeans.TPP_ID);
        typedAuthMeans.put(CLIENT_ID_NAME, TypedAuthenticationMeans.CLIENT_ID_STRING);
        typedAuthMeans.put(APPLICATION_NAME_NAME, new TypedAuthenticationMeans(
                APPLICATION_NAME_NAME, StringType.getInstance(), RenderingType.ONE_LINE_STRING
        ));
        typedAuthMeans.put(APPLICATION_DESCRIPTION_NAME, new TypedAuthenticationMeans(
                APPLICATION_NAME_NAME, StringType.getInstance(), RenderingType.MULTI_LINE_STRING
        ));
        return typedAuthMeans;
    }

    @Override
    public Map<String, TypedAuthenticationMeans> getAutoConfigureMeans() {
        Map<String, TypedAuthenticationMeans> autoonboardingTypedAuthenticationMeansMap = new HashMap<>();
        autoonboardingTypedAuthenticationMeansMap.put(TPP_ID_NAME, TypedAuthenticationMeans.TPP_ID);
        autoonboardingTypedAuthenticationMeansMap.put(CLIENT_ID_NAME, TypedAuthenticationMeans.CLIENT_ID_STRING);
        return autoonboardingTypedAuthenticationMeansMap;
    }

    @Override
    public HandelsbankenGroupAuthMeans createAuthenticationMeans(Map<String, BasicAuthenticationMean> authMeans, String providerIdentifier) {
        return HandelsbankenGroupAuthMeans.builder()
                .transportCertificate(getCertificate(authMeans, providerIdentifier))
                .transportKeyId(getValue(authMeans, providerIdentifier, TRANSPORT_KEY_ID_NAME))
                .tppId(getNullableValue(authMeans, TPP_ID_NAME))
                .clientId(getNullableValue(authMeans, CLIENT_ID_NAME))
                .appName(getValue(authMeans, providerIdentifier, APPLICATION_NAME_NAME))
                .appDescription(getValue(authMeans, providerIdentifier, APPLICATION_DESCRIPTION_NAME))
                .build();
    }

    @Override
    public Optional<KeyRequirements> getTransportKeyRequirements() {
        return HsmEidasUtils.getKeyRequirements(TRANSPORT_KEY_ID_NAME, TRANSPORT_CERTIFICATE_NAME);
    }

    private X509Certificate getCertificate(Map<String, BasicAuthenticationMean> authMeans,
                                           String providerIdentifier) {
        String authValue = getValue(authMeans, providerIdentifier, HandelsbankenGroupAuthMeansProducerV1.TRANSPORT_CERTIFICATE_NAME);
        try {
            return KeyUtil.createCertificateFromPemFormat(authValue);
        } catch (CertificateException e) {
            throw new InvalidAuthenticationMeansException(providerIdentifier, HandelsbankenGroupAuthMeansProducerV1.TRANSPORT_CERTIFICATE_NAME, "Cannot process certificate for thumbprint");
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

    private String getNullableValue(Map<String, BasicAuthenticationMean> authMeans,
                                    String authKey) {
        var authValue = authMeans.get(authKey);
        if (authValue == null) {
            return null;
        }
        return authValue.getValue();
    }

}
