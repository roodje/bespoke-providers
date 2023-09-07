package com.yolt.providers.monorepogroup.olbgroup.common.auth;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;

import java.util.Map;
import java.util.Optional;

public interface OlbGroupAuthenticationMeansProducer {

    Map<String, TypedAuthenticationMeans> getTypedAuthenticationMeans();

    OlbGroupAuthenticationMeans createAuthenticationMeans(Map<String, BasicAuthenticationMean> authMeans, String providerIdentifier);

    Optional<KeyRequirements> getTransportKeyRequirements();
}
