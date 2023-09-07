package com.yolt.providers.unicredit.common.service;

import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequest;
import com.yolt.providers.common.ais.url.UrlGetLoginRequest;
import com.yolt.providers.common.ais.url.UrlRefreshAccessMeansRequest;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.Step;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.unicredit.common.util.ProviderInfo;
import nl.ing.lovebird.providershared.AccessMeansDTO;

public interface UniCreditAuthorizationService {
    Step getLoginInfo(final UrlGetLoginRequest urlGetLogin, final ProviderInfo providerInfo) throws TokenInvalidException;
    AccessMeansOrStepDTO createNewAccessMeans(final UrlCreateAccessMeansRequest urlCreateAccessMeans, final ProviderInfo providerInfo);
    AccessMeansDTO refreshAccessMeans(final UrlRefreshAccessMeansRequest urlRefreshAccessMeans, final ProviderInfo providerInfo) throws TokenInvalidException;
}
