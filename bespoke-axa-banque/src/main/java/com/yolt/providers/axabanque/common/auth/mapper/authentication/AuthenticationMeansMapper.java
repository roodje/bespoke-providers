package com.yolt.providers.axabanque.common.auth.mapper.authentication;

import com.yolt.providers.axabanque.common.auth.GroupAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;

import java.util.Map;

public interface AuthenticationMeansMapper {
    GroupAuthenticationMeans map(Map<String, BasicAuthenticationMean> authenticationMeans, String providerIdentifier);
}
