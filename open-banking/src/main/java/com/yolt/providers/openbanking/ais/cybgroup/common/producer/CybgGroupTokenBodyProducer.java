package com.yolt.providers.openbanking.ais.cybgroup.common.producer;

import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import com.yolt.providers.openbanking.ais.generic2.oauth2.tokenbodysupplier.BasicOauthTokenBodyProducer;
import org.springframework.util.MultiValueMap;

import static com.yolt.providers.common.constants.OAuth.SCOPE;

public class CybgGroupTokenBodyProducer extends BasicOauthTokenBodyProducer {

    private final TokenScope scope;

    public CybgGroupTokenBodyProducer(final TokenScope scope) {
        this.scope = scope;
    }

    @Override
    public MultiValueMap<String, String> getRefreshAccessTokenBody(DefaultAuthMeans authenticationMeans,
                                                                   String refreshToken,
                                                                   String... args) {
        MultiValueMap<String, String> body = super.getRefreshAccessTokenBody(authenticationMeans,
                refreshToken,
                args);
        body.add(SCOPE, scope.getAuthorizationUrlScope());
        return body;
    }

    @Override
    public MultiValueMap<String, String> getCreateAccessTokenBody(DefaultAuthMeans authenticationMeans,
                                                                  String authorizationCode,
                                                                  String redirectURI,
                                                                  String... args) {
        MultiValueMap<String, String> body = super.getCreateAccessTokenBody(authenticationMeans,
                authorizationCode,
                redirectURI,
                args);
        body.add(SCOPE, scope.getAuthorizationUrlScope());
        return body;
    }
}