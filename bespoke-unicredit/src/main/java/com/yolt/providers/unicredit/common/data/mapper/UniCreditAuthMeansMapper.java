package com.yolt.providers.unicredit.common.data.mapper;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.unicredit.common.auth.UniCreditAuthMeans;

import java.util.Map;

public interface UniCreditAuthMeansMapper {
    UniCreditAuthMeans fromBasicAuthenticationMeans(final Map<String, BasicAuthenticationMean> basicAuthenticationMeans, final String provider);
}
