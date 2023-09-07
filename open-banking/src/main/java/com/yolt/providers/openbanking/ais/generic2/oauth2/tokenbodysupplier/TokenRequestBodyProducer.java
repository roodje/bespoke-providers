package com.yolt.providers.openbanking.ais.generic2.oauth2.tokenbodysupplier;

import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;

public interface TokenRequestBodyProducer<T> {
    T getRefreshAccessTokenBody(DefaultAuthMeans authenticationMeans, String refreshToken, String... args);

    T getCreateAccessTokenBody(DefaultAuthMeans authenticationMeans, String authorizationCode, String redirectURI, String... args);

    T getCreateClientCredentialsBody(DefaultAuthMeans authenticationMeans, TokenScope scope, String... args);
}
