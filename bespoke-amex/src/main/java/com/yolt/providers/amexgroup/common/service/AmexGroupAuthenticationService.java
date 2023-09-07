package com.yolt.providers.amexgroup.common.service;

import com.yolt.providers.amexgroup.common.auth.AmexGroupAuthMeans;
import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequest;
import com.yolt.providers.common.ais.url.UrlGetLoginRequest;
import com.yolt.providers.common.ais.url.UrlOnUserSiteDeleteRequest;
import com.yolt.providers.common.ais.url.UrlRefreshAccessMeansRequest;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.exception.TokenInvalidException;
import nl.ing.lovebird.providershared.AccessMeansDTO;

public interface AmexGroupAuthenticationService {
    String getLoginInfo(AmexGroupAuthMeans amexGroupAuthMeans,
                        UrlGetLoginRequest urlGetLogin);

    AccessMeansOrStepDTO createNewAccessMeansFromCsv(AmexGroupAuthMeans amexGroupAuthMeans,
                                                     UrlCreateAccessMeansRequest urlCreateAccessMeans,
                                                     String csvAuthorizationCodes);

    AccessMeansDTO getRefreshAccessMeans(UrlRefreshAccessMeansRequest urlRefreshAccessMeans,
                                         AmexGroupAuthMeans amexGroupAuthMeans) throws TokenInvalidException;

    void revokeUserToken(UrlOnUserSiteDeleteRequest request, AmexGroupAuthMeans amexGroupAuthMeans)
            throws TokenInvalidException;
}
