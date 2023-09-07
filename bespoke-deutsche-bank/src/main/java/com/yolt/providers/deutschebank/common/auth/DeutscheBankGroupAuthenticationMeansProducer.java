package com.yolt.providers.deutschebank.common.auth;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;

import java.util.Map;
import java.util.Optional;

public interface DeutscheBankGroupAuthenticationMeansProducer {

    Map<String, TypedAuthenticationMeans> getTypedAuthenticationMeans();

    DeutscheBankGroupAuthenticationMeans createAuthenticationMeans(Map<String, BasicAuthenticationMean> authMeans, String providerIdentifier);

    Optional<KeyRequirements> getTransportKeyRequirements();
}
