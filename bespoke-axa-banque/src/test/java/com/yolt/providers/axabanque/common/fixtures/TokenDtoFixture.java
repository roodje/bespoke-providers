package com.yolt.providers.axabanque.common.fixtures;

import com.yolt.providers.axabanque.common.model.external.Token;
import com.yolt.providers.axabanque.common.model.internal.AccessToken;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TokenDtoFixture {

    public static Token createTokenDtoMock(String tokenType, String refreshToken, String scope, Long expiresIn, String token) {
        Token tokenDto = mock(Token.class);
        when(tokenDto.getTokenType()).thenReturn(tokenType);
        when(tokenDto.getRefreshToken()).thenReturn(refreshToken);
        when(tokenDto.getScope()).thenReturn(scope);
        when(tokenDto.getExpiresIn()).thenReturn(expiresIn);
        when(tokenDto.getAccessToken()).thenReturn(token);
        return tokenDto;
    }

    public static AccessToken createAccessToken(Long expiresIn, String refreshToken, String scope, String tokenType, String token) {
        return new AccessToken(expiresIn, refreshToken, scope, tokenType, token);
    }
}
