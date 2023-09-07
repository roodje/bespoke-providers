package com.yolt.providers.monorepogroup.libragroup.common.ais.auth;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.monorepogroup.libragroup.common.LibraGroupAuthenticationMeans;
import com.yolt.providers.monorepogroup.libragroup.common.ais.auth.dto.LibraGroupAuthData;
import com.yolt.providers.monorepogroup.libragroup.common.ais.auth.dto.LibraGroupConsent;

public interface LibraGroupAuthenticationHttpClient {
    String getClientCredentialsToken(String clientId,
                                     String clientSecret);

    LibraGroupConsent getConsent(LibraGroupAuthenticationMeans.SigningData signingData,
                                 String accessToken,
                                 Signer signer,
                                 String providerIdentifier);

    void deleteConsent(String consentId,
                       LibraGroupAuthenticationMeans.SigningData signingData,
                       Signer signer) throws TokenInvalidException;

    LibraGroupAuthData getUserToken(String clientId,
                                    String clientSecret,
                                    String authCode,
                                    String redirectUri,
                                    String consentId,
                                    String providerIdentifier);

    LibraGroupAuthData refreshUserToken(String clientId,
                                        String clientSecret,
                                        String refreshToken,
                                        String redirectUrl,
                                        String providerIdentifier) throws TokenInvalidException;
}
