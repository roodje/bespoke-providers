package com.yolt.providers.monorepogroup.chebancagroup.common.service;

import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequest;
import com.yolt.providers.common.ais.url.UrlRefreshAccessMeansRequest;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.monorepogroup.chebancagroup.common.auth.CheBancaGroupAuthenticationMeans;
import com.yolt.providers.monorepogroup.chebancagroup.common.http.CheBancaGroupHttpClient;
import nl.ing.lovebird.providershared.AccessMeansDTO;

public interface CheBancaGroupAuthorizationService {
    RedirectStep getLoginInfo(final CheBancaGroupHttpClient httpClient, final Signer signer, final CheBancaGroupAuthenticationMeans authenticationMeans, final String baseClientRedirectUrl, final String state);

    AccessMeansOrStepDTO createNewAccessMeans(final Signer signer, CheBancaGroupHttpClient httpClient, final CheBancaGroupAuthenticationMeans authenticationMeans, final UrlCreateAccessMeansRequest urlCreateAccessMeans);

    AccessMeansDTO refreshAccessMeans(final Signer signer, CheBancaGroupHttpClient httpClient, final CheBancaGroupAuthenticationMeans authenticationMeans, final UrlRefreshAccessMeansRequest urlRefreshAccessMeansRequest);
}
