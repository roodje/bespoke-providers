package com.yolt.providers.monorepogroup.qontogroup.common.mapper.auth;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.monorepogroup.qontogroup.common.QontoGroupAuthenticationMeans;

import java.util.Map;

public interface QontoGroupAuthenticationMeansMapper {
    QontoGroupAuthenticationMeans map(final Map<String, BasicAuthenticationMean> authenticationMeans, final String providerIdentifier);
}
