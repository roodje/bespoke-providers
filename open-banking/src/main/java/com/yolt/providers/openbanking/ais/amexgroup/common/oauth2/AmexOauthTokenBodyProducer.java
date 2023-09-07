package com.yolt.providers.openbanking.ais.amexgroup.common.oauth2;

import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import com.yolt.providers.openbanking.ais.generic2.oauth2.tokenbodysupplier.TokenRequestBodyProducer;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static com.yolt.providers.common.constants.OAuth.*;

public class AmexOauthTokenBodyProducer implements TokenRequestBodyProducer<MultiValueMap<String, String>> {

    @Override
    public MultiValueMap<String, String> getRefreshAccessTokenBody(DefaultAuthMeans authenticationMeans, String refreshToken, String... args) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add(CLIENT_ID, authenticationMeans.getClientId());
        body.add(CLIENT_SECRET, authenticationMeans.getClientSecret());
        body.add(GRANT_TYPE, REFRESH_TOKEN);
        body.add(REFRESH_TOKEN, refreshToken);
        return body;
    }

    @Override
    public MultiValueMap<String, String> getCreateAccessTokenBody(DefaultAuthMeans authenticationMeans, String authorizationCode, String redirectURI, String... args) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add(CLIENT_ID, authenticationMeans.getClientId());
        body.add(CLIENT_SECRET, authenticationMeans.getClientSecret());
        body.add(GRANT_TYPE, AUTHORIZATION_CODE);
        body.add(CODE, authorizationCode);
        body.add(SCOPE, args[0]);
        body.add(REDIRECT_URI, redirectURI);
        body.add("code_verifier", args[1]);
        return body;
    }

    @Override
    public MultiValueMap<String, String> getCreateClientCredentialsBody(DefaultAuthMeans authenticationMeans, TokenScope scope, String... args) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add(CLIENT_ID, authenticationMeans.getClientId());
        body.add(CLIENT_SECRET, authenticationMeans.getClientSecret());
        body.add(GRANT_TYPE, CLIENT_CREDENTIALS);
        body.add(SCOPE, scope.getGrantScope());
        return body;
    }
}
