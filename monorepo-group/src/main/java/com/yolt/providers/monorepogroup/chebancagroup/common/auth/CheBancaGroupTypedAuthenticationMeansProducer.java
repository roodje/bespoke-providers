package com.yolt.providers.monorepogroup.chebancagroup.common.auth;

import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;

import java.util.Map;
import java.util.Optional;

public interface CheBancaGroupTypedAuthenticationMeansProducer {

    Map<String, TypedAuthenticationMeans> getTypedAuthenticationMeans();

    Map<String, TypedAuthenticationMeans> getAutoconfigureTypedAuthenticationMeans();

    Optional<KeyRequirements> getTransportKeyRequirements();

    Optional<KeyRequirements> getSigningKeyRequirements();
}
