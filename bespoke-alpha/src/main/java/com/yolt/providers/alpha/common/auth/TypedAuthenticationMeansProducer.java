package com.yolt.providers.alpha.common.auth;

import com.yolt.providers.alpha.common.auth.dto.AlphaAuthMeans;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;

import java.util.Map;

public interface TypedAuthenticationMeansProducer {
    Map<String, TypedAuthenticationMeans> getTypedAuthenticationMeans();

    AlphaAuthMeans createAuthenticationMeans(final Map<String, BasicAuthenticationMean> typedAuthenticationMeans, String providerIdentifier);
}
