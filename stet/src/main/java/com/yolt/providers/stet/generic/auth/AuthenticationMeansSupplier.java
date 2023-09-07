package com.yolt.providers.stet.generic.auth;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;

import java.util.Map;
import java.util.Optional;

public interface AuthenticationMeansSupplier {

    Map<String, TypedAuthenticationMeans> getTypedAuthMeans();

    DefaultAuthenticationMeans getAuthMeans(Map<String, BasicAuthenticationMean> basicAuthMeans, String providerIdentifier);

    Optional<KeyRequirements> getTransportKeyRequirements();

    Optional<KeyRequirements> getSigningKeyRequirements();
}
