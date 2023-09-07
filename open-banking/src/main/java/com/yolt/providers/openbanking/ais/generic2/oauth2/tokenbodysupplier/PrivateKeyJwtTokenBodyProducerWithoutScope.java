package com.yolt.providers.openbanking.ais.generic2.oauth2.tokenbodysupplier;

import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import lombok.AllArgsConstructor;
import org.springframework.util.MultiValueMap;

import static com.yolt.providers.common.constants.OAuth.SCOPE;

@AllArgsConstructor
public class PrivateKeyJwtTokenBodyProducerWithoutScope extends PrivateKeyJwtTokenBodyProducer {

    @Override
    public MultiValueMap<String, String> getRefreshAccessTokenBody(DefaultAuthMeans authenticationMeans,
                                                                   String refreshToken,
                                                                   String... args) {
        MultiValueMap<String, String> body = super.getRefreshAccessTokenBody(authenticationMeans, refreshToken, args);
        body.remove(SCOPE);
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
        body.remove(SCOPE);
        return body;
    }
}
