package com.yolt.providers.axabanque.common.fixtures;

import com.yolt.providers.axabanque.common.model.internal.AccessToken;
import com.yolt.providers.axabanque.common.model.internal.GroupAccessMeans;
import com.yolt.providers.axabanque.common.model.internal.GroupProviderState;

import java.time.Instant;

public class AccessMeansFixture {
    public static GroupAccessMeans createAccessMeans(String consentId, String token, String traceId) {
        return createAccessMeans("", "", "", consentId, traceId, 1L,
                "", "", "", token);
    }

    public static GroupAccessMeans createAccessMeans(String redirectUri, String codeVerifier, String code, String consentId, String traceId,
                                                     long tokenExpiresIn, String refreshToken, String scope, String tokenType, String token) {
        Instant aDate = Instant.parse("1950-05-30T18:35:24.00Z");
        GroupProviderState axaGroupProviderState = new GroupProviderState(codeVerifier, code, consentId, traceId, aDate.toEpochMilli());
        AccessToken accessToken = new AccessToken(tokenExpiresIn, refreshToken, scope, tokenType, token);
        return new GroupAccessMeans(redirectUri, axaGroupProviderState, accessToken);
    }
}
