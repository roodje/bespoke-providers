package com.yolt.providers.amexgroup.common.auth;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;

import java.util.Map;

public interface AmexGroupAuthMeanProducer {

    AmexGroupAuthMeans createAuthMeans(Map<String, BasicAuthenticationMean> authenticationMeans, String providerIdentifier);
}
