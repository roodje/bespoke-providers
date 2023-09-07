package com.yolt.providers.alpha.common.auth.service;

import com.yolt.providers.alpha.common.auth.dto.AlphaAuthMeans;
import com.yolt.providers.alpha.common.auth.dto.AlphaToken;
import com.yolt.providers.alpha.common.http.AlphaHttpClient;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.TokenInvalidException;

public interface AuthenticationService {
    String getLoginInfo(final AlphaAuthMeans authMeans, final AlphaHttpClient httpClient, final String baseClientRedirectUrl, final String state, final Signer signer) throws TokenInvalidException;

    AlphaToken createNewAccessMeans(AlphaAuthMeans authMeans, AlphaHttpClient httpClient, final String redirectUrlPostedBackFromSite, final String baseClientRedirectUrl) throws TokenInvalidException;

    AlphaToken refreshAccessMeans(AlphaAuthMeans authMeans, AlphaHttpClient httpClient, String refreshToken) throws TokenInvalidException;
}
