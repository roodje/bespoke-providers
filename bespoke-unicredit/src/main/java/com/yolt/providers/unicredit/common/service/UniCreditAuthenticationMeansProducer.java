package com.yolt.providers.unicredit.common.service;

import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;

import java.util.Map;
import java.util.Optional;

public interface UniCreditAuthenticationMeansProducer {
    Map<String, TypedAuthenticationMeans> getAutoConfiguredMeans();
    Map<String, TypedAuthenticationMeans> getTypedAuthenticationMeans();
    Optional<KeyRequirements> getTransportKeyRequirements();
}
