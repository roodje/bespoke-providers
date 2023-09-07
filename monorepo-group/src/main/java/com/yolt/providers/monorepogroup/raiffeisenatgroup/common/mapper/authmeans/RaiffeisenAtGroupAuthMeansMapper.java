package com.yolt.providers.monorepogroup.raiffeisenatgroup.common.mapper.authmeans;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.RaiffeisenAtGroupAuthenticationMeans;

import java.util.Map;

public interface RaiffeisenAtGroupAuthMeansMapper {

    RaiffeisenAtGroupAuthenticationMeans map(final Map<String, BasicAuthenticationMean> authenticationMean, final String providerIdentifier);

    RaiffeisenAtGroupAuthenticationMeans mapForAutoonboarding(final Map<String, BasicAuthenticationMean> authenticationMean, final String providerIdentifier);

}
