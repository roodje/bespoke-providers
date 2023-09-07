package com.yolt.providers.axabanque.common.auth.mapper.access;

import com.yolt.providers.axabanque.common.model.external.Token;
import com.yolt.providers.axabanque.common.model.internal.AccessToken;

import java.util.function.Function;

public class DefaultAccessTokenMapper implements Function<Token, AccessToken> {

    @Override
    public AccessToken apply(Token token) {
        return new AccessToken(token.getExpiresIn(), token.getRefreshToken(), token.getScope(), token.getTokenType(), token.getAccessToken());
    }
}