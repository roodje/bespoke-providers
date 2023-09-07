package com.yolt.providers.monorepogroup.cecgroup.common.service;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.monorepogroup.cecgroup.common.CecGroupAccessMeans;
import com.yolt.providers.monorepogroup.cecgroup.common.auth.CecGroupAuthenticationMeans;
import com.yolt.providers.monorepogroup.cecgroup.common.http.CecGroupHttpClient;

public interface CecGroupAuthorizationService {

    String getConsentId(CecGroupHttpClient httpClient,
                        CecGroupAuthenticationMeans authMeans,
                        Signer signer,
                        String psuIpAddress,
                        String redirectUrl,
                        String state);

    CecGroupAccessMeans createAccessMeans(CecGroupHttpClient httpClient,
                                          String clientId,
                                          String clientSecret,
                                          String redirectUri,
                                          String authCode,
                                          String consentId);

    CecGroupAccessMeans refreshAccessMeans(CecGroupHttpClient httpClient,
                                           String clientId,
                                           CecGroupAccessMeans oldAccessMeans);
}
