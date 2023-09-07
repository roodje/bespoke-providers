package com.yolt.providers.openbanking.ais.lloydsbankinggroup.common.oauth2;

import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import com.yolt.providers.openbanking.ais.generic2.oauth2.tokenbodysupplier.BasicOauthTokenBodyProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static com.yolt.providers.common.constants.OAuth.*;

@RequiredArgsConstructor
public class LloydsBasicOauthTokenBodyProducerDecorator extends BasicOauthTokenBodyProducer {

    @Override
    public MultiValueMap<String, String> getRefreshAccessTokenBody(DefaultAuthMeans authenticationMeans, String refreshToken, String... args) {
        MultiValueMap<String, String> body = super.getRefreshAccessTokenBody(authenticationMeans, refreshToken, args);
        body.add(CLIENT_ID, authenticationMeans.getClientId());
        return body;
    }

    @Override
    public MultiValueMap<String, String> getCreateAccessTokenBody(DefaultAuthMeans authenticationMeans, String authorizationCode, String redirectURI, String... args) {
        MultiValueMap<String, String> body = super.getCreateAccessTokenBody(authenticationMeans, authorizationCode, redirectURI, args);
        body.add(CLIENT_ID, authenticationMeans.getClientId());
        return body;
    }

    @Override
    public MultiValueMap<String, String> getCreateClientCredentialsBody(DefaultAuthMeans authenticationMeans, TokenScope scope, String... args) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add(GRANT_TYPE, "client_credentials");
        body.add(SCOPE, scope.getAuthorizationUrlScope());
        body.add(CLIENT_ID, authenticationMeans.getClientId());
        return body;
    }
}
