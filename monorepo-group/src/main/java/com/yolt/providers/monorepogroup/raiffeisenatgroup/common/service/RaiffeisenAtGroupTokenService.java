package com.yolt.providers.monorepogroup.raiffeisenatgroup.common.service;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.RaiffeisenAtGroupAuthenticationMeans;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.http.RaiffeisenAtGroupHttpClient;

public interface RaiffeisenAtGroupTokenService {
    String createClientCredentialToken(final RaiffeisenAtGroupHttpClient httpClient, final RaiffeisenAtGroupAuthenticationMeans authenticationMeans) throws TokenInvalidException;
}
