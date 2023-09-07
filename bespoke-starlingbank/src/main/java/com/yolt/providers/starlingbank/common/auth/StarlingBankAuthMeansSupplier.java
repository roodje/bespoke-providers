package com.yolt.providers.starlingbank.common.auth;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;

import java.util.Map;

public interface StarlingBankAuthMeansSupplier {
    StarlingBankAuthenticationMeans createAuthenticationMeans(Map<String, BasicAuthenticationMean> basicAuthMeans,
                                                              String providerIdentifier);
}
