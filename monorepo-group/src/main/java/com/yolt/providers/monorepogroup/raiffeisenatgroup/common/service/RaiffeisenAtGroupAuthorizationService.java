package com.yolt.providers.monorepogroup.raiffeisenatgroup.common.service;

import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.RaiffeisenAtGroupAuthenticationMeans;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.http.RaiffeisenAtGroupHttpClient;

import java.util.UUID;

public interface RaiffeisenAtGroupAuthorizationService {
    RedirectStep getLoginInfo(final RaiffeisenAtGroupHttpClient httpClient, final RaiffeisenAtGroupAuthenticationMeans authenticationMeans, final String baseClientRedirectUrl, String psuIpAddress, String state);

    AccessMeansOrStepDTO createNewAccessMeans(final RaiffeisenAtGroupHttpClient httpClient, final RaiffeisenAtGroupAuthenticationMeans authenticationMeans, final String redirectUrlPostedBackFromSite, final UUID userId, final String psuIpAddress, final String providerState);

    void deleteUserConsent(final RaiffeisenAtGroupHttpClient httpClient, final RaiffeisenAtGroupAuthenticationMeans authenticationMeans, final String psuIpAddress, final String externalConsentId) throws TokenInvalidException;
}
