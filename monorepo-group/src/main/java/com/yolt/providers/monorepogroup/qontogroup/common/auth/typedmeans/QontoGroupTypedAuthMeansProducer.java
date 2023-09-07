package com.yolt.providers.monorepogroup.qontogroup.common.auth.typedmeans;

import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;

import java.util.Map;
import java.util.Optional;

public interface QontoGroupTypedAuthMeansProducer {

    Map<String, TypedAuthenticationMeans> getTypedAuthenticationMeans();

    Optional<KeyRequirements> getSigningKeyRequirements();
}
