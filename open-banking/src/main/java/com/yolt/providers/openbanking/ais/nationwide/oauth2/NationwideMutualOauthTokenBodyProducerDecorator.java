package com.yolt.providers.openbanking.ais.nationwide.oauth2;

import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import com.yolt.providers.openbanking.ais.generic2.oauth2.tokenbodysupplier.TokenRequestBodyProducer;
import lombok.AllArgsConstructor;
import org.springframework.util.MultiValueMap;

import static com.yolt.providers.common.constants.OAuth.CLIENT_ID;

@AllArgsConstructor
public class NationwideMutualOauthTokenBodyProducerDecorator implements TokenRequestBodyProducer {

    private final TokenRequestBodyProducer<MultiValueMap<String, String>> wrappee;

    @Override
    public MultiValueMap<String, String> getRefreshAccessTokenBody(DefaultAuthMeans authenticationMeans, String refreshToken, String... args) {
        MultiValueMap<String, String> body = wrappee.getRefreshAccessTokenBody(authenticationMeans, refreshToken, args);
        return addClientIdToBody(body, authenticationMeans.getClientId());
    }

    @Override
    public MultiValueMap<String, String> getCreateAccessTokenBody(DefaultAuthMeans authenticationMeans, String authorizationCode, String redirectURI, String... args) {
        MultiValueMap<String, String> body = wrappee.getCreateAccessTokenBody(authenticationMeans, authorizationCode, redirectURI, args);
        return addClientIdToBody(body, authenticationMeans.getClientId());
    }

    @Override
    public MultiValueMap<String, String> getCreateClientCredentialsBody(DefaultAuthMeans authenticationMeans, TokenScope scope, String... args) {
        MultiValueMap<String, String> body = wrappee.getCreateClientCredentialsBody(authenticationMeans, scope, args);
        return addClientIdToBody(body, authenticationMeans.getClientId());
    }

    private MultiValueMap<String, String> addClientIdToBody(MultiValueMap<String, String> body, String clientId) {
        body.add(CLIENT_ID, clientId);
        return body;
    }
}
