package com.yolt.providers.monorepogroup.chebancagroup.common.mapper.authmeans;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.monorepogroup.chebancagroup.common.auth.CheBancaGroupAuthenticationMeans;

import java.util.Map;

public interface CheBancaGroupAuthMeansMapper {

    CheBancaGroupAuthenticationMeans map(final Map<String, BasicAuthenticationMean> authenticationMean, final String providerIdentifier);

}
