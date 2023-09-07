package com.yolt.providers.dkbgroup.common.auth;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;

import java.util.Map;

public interface TypedAuthenticationMeansProducer {
    Map<String, TypedAuthenticationMeans> getTypedAuthenticationMeans();

    DKBGroupAuthMeans createAuthenticationMeans(final Map<String, BasicAuthenticationMean> typedAuthenticationMeans, String providerIdentifier);
}
