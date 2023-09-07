package com.yolt.providers.openbanking.ais.generic2.oauth2.tokenbodysupplier;

import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static com.yolt.providers.common.constants.OAuth.*;

public class BasicOauthTokenBodyProducer implements TokenRequestBodyProducer {
    @Override
    public MultiValueMap<String, String> getRefreshAccessTokenBody(DefaultAuthMeans authenticationMeans,
                                                                   String refreshToken,
                                                                   String... args) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add(GRANT_TYPE, "refresh_token");
        body.add(REFRESH_TOKEN, refreshToken);
        return body;
    }

    @Override
    public MultiValueMap<String, String> getCreateAccessTokenBody(DefaultAuthMeans authenticationMeans,
                                                                  String authorizationCode,
                                                                  String redirectURI,
                                                                  String... args) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add(GRANT_TYPE, "authorization_code");
        body.add(REDIRECT_URI, redirectURI);
        body.add(CODE, authorizationCode);
        return body;
    }

    @Override
    public MultiValueMap<String, String> getCreateClientCredentialsBody(DefaultAuthMeans authenticationMeans, TokenScope scope,
                                                                        String... args) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add(GRANT_TYPE, "client_credentials");
        body.add(SCOPE, scope.getGrantScope());
        return body;
    }
}
