package com.yolt.providers.n26.common.auth;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;

import java.util.Map;
import java.util.Optional;

public interface N26GroupAuthenticationMeansProducer {

    Map<String, TypedAuthenticationMeans> getTypedAuthenticationMeans();

    N26GroupAuthenticationMeans createAuthenticationMeans(Map<String, BasicAuthenticationMean> authMeans, String providerIdentifier);

    Optional<KeyRequirements> getTransportKeyRequirements();
}
