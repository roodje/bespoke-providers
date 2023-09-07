package com.yolt.providers.monorepogroup.libragroup.common.ais.auth;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.monorepogroup.libragroup.common.LibraGroupAuthenticationMeans;
import com.yolt.providers.monorepogroup.libragroup.common.ais.auth.dto.LibraGroupAccessMeans;
import com.yolt.providers.monorepogroup.libragroup.common.ais.auth.dto.LibraLoginUrlData;

public interface LibraGroupAuthenticationService {
    LibraLoginUrlData getLoginUrlData(LibraGroupAuthenticationMeans authenticationMeans,
                                      RestTemplateManager restTemplateManager,
                                      String redirectUri,
                                      String loginState,
                                      Signer signer);

    LibraGroupAccessMeans getUserToken(LibraGroupAuthenticationMeans authenticationMeans,
                                       String redirectUrl,
                                       String consentId,
                                       RestTemplateManager restTemplateManager,
                                       String authCode);

    LibraGroupAccessMeans refreshUserToken(LibraGroupAuthenticationMeans authenticationMeans,
                                           String refreshToken,
                                           String redirectUrl,
                                           String consentId,
                                           RestTemplateManager restTemplateManager) throws TokenInvalidException;

    void deleteConsent(LibraGroupAuthenticationMeans.SigningData signingData,
                       RestTemplateManager restTemplateManager,
                       String consentId,
                       Signer signer) throws TokenInvalidException;
}
