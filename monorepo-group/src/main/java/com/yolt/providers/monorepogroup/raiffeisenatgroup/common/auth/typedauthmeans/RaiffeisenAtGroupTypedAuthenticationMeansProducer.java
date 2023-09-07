package com.yolt.providers.monorepogroup.raiffeisenatgroup.common.auth.typedauthmeans;

import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;

import java.util.Map;
import java.util.Optional;

public interface RaiffeisenAtGroupTypedAuthenticationMeansProducer {

    Map<String, TypedAuthenticationMeans> getTypedAuthenticationMeans();

    Map<String, TypedAuthenticationMeans> getAutoconfigureTypedAuthenticationMeans();

    Optional<KeyRequirements> getTransportKeyRequirements();
}
