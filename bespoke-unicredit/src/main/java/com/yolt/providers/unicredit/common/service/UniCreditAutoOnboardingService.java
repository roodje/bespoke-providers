package com.yolt.providers.unicredit.common.service;

import com.yolt.providers.common.ais.url.UrlAutoOnboardingRequest;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.unicredit.common.util.ProviderInfo;

import java.util.Map;

public interface UniCreditAutoOnboardingService {
    Map<String, BasicAuthenticationMean> autoConfigureMeans(final UrlAutoOnboardingRequest urlAutoOnboardingRequest, final ProviderInfo providerInfo) throws TokenInvalidException;
}
