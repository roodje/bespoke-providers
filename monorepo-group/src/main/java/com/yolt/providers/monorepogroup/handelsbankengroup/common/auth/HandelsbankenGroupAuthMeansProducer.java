package com.yolt.providers.monorepogroup.handelsbankengroup.common.auth;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;

import java.util.Map;
import java.util.Optional;

public interface HandelsbankenGroupAuthMeansProducer {

    Map<String, TypedAuthenticationMeans> getTypedAuthenticationMeans();

    Map<String, TypedAuthenticationMeans> getAutoConfigureMeans();

    HandelsbankenGroupAuthMeans createAuthenticationMeans(Map<String, BasicAuthenticationMean> authMeans, String providerIdentifier);

    Optional<KeyRequirements> getTransportKeyRequirements();
}
