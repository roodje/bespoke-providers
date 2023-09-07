package com.yolt.providers.openbanking.ais.revolutgroup.common.service.autoonboarding;

import com.yolt.providers.common.ais.url.UrlAutoOnboardingRequest;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;

import java.util.Optional;

public interface RevolutGroupAutoOnboardingServiceV2 {

    Optional<RevolutAutoOnboardingResponse> register(final HttpClient httpClient,
                                                     final UrlAutoOnboardingRequest urlAutoOnboardingRequest,
                                                     final DefaultAuthMeans defaultAuthMeans) throws TokenInvalidException;

    void removeAutoConfiguration(final HttpClient httpClient,
                                 final UrlAutoOnboardingRequest urlAutoOnboardingRequest,
                                 final DefaultAuthMeans defaultAuthMeans) throws TokenInvalidException;
}
