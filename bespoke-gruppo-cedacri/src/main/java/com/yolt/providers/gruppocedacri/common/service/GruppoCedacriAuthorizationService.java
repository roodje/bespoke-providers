package com.yolt.providers.gruppocedacri.common.service;

import com.yolt.providers.gruppocedacri.common.GruppoCedacriAccessMeans;
import com.yolt.providers.gruppocedacri.common.dto.consent.ConsentResponse;
import com.yolt.providers.gruppocedacri.common.dto.token.TokenResponse;
import com.yolt.providers.gruppocedacri.common.http.GruppoCedacriHttpClient;

public interface GruppoCedacriAuthorizationService {

    TokenResponse getAccessToken(GruppoCedacriHttpClient httpClient,
                                 String redirectUrlPostedBackFromSite,
                                 String redirectUrl,
                                 String clientId,
                                 String clientSecret);

    String getAuthorizationUrl(GruppoCedacriHttpClient httpClient,
                               String authorizationToken,
                               String redirectUrl,
                               String psuIpAddress,
                               String state,
                               String clientId);

    ConsentResponse createConsent(GruppoCedacriHttpClient httpClient,
                                  String authorizationToken,
                                  String redirectUrl,
                                  String psuIpAddress,
                                  String state);

    void deleteConsent(GruppoCedacriHttpClient httpClient,
                       GruppoCedacriAccessMeans accessMeans);
}
