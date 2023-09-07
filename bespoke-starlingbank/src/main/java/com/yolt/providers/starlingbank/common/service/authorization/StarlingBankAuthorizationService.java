package com.yolt.providers.starlingbank.common.service.authorization;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.starlingbank.common.auth.StarlingBankAuthenticationMeans;
import com.yolt.providers.starlingbank.common.http.StarlingBankHttpClient;
import com.yolt.providers.starlingbank.common.model.domain.Token;
import nl.ing.lovebird.providershared.AccessMeansDTO;

import java.util.UUID;

public interface StarlingBankAuthorizationService {
    String getLoginUrl(String redirectUrl, String loginState, StarlingBankAuthenticationMeans authMeans);

    AccessMeansDTO createAccessMeans(StarlingBankHttpClient httpClient,
                                     StarlingBankAuthenticationMeans authMeans,
                                     String redirectUrlPostedBackFromSite,
                                     UUID userId);

    Token getOAuthToken(StarlingBankHttpClient httpClient,
                        StarlingBankAuthenticationMeans authMeans,
                        String redirectUrlPostedBackFromSite);

    Token getOAuthRefreshToken(StarlingBankHttpClient httpClient,
                               String refreshToken,
                               StarlingBankAuthenticationMeans authMeans) throws TokenInvalidException;

    AccessMeansDTO refreshAccessMeans(StarlingBankHttpClient httpClient,
                                      AccessMeansDTO accessMeansDTO,
                                      StarlingBankAuthenticationMeans authMeans) throws TokenInvalidException;
}
