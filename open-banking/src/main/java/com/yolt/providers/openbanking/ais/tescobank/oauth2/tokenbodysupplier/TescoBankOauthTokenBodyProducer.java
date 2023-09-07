package com.yolt.providers.openbanking.ais.tescobank.oauth2.tokenbodysupplier;

import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import com.yolt.providers.openbanking.ais.generic2.oauth2.tokenbodysupplier.BasicOauthTokenBodyProducer;
import lombok.AllArgsConstructor;
import org.springframework.util.MultiValueMap;

import static com.yolt.providers.common.constants.OAuth.CLIENT_ID;
import static com.yolt.providers.common.constants.OAuth.SCOPE;

@AllArgsConstructor
public class TescoBankOauthTokenBodyProducer extends BasicOauthTokenBodyProducer {

    @Override
    public MultiValueMap<String, String> getCreateAccessTokenBody(final DefaultAuthMeans authenticationMeans,
                                                                  final String authorizationCode,
                                                                  final String redirectURI,
                                                                  final String... args) {

        MultiValueMap<String, String> body = super.getCreateAccessTokenBody(authenticationMeans, authorizationCode, redirectURI, args);
        body.add(CLIENT_ID, authenticationMeans.getClientId());
        body.remove(SCOPE);
        return body;
    }

    @Override
    public MultiValueMap<String, String> getRefreshAccessTokenBody(final DefaultAuthMeans authenticationMeans,
                                                                   final String refreshToken,
                                                                   final String... args) {
        MultiValueMap<String, String> body = super.getRefreshAccessTokenBody(authenticationMeans, refreshToken, args);
        body.add(CLIENT_ID, authenticationMeans.getClientId());
        body.remove(SCOPE);
        return body;
    }

    @Override
    public MultiValueMap<String, String> getCreateClientCredentialsBody(final DefaultAuthMeans authenticationMeans,
                                                                        final TokenScope scope,
                                                                        final String... args) {

        MultiValueMap<String, String> body = super.getCreateClientCredentialsBody(authenticationMeans, scope, args);
        body.add(CLIENT_ID, authenticationMeans.getClientId());
        return body;
    }

}
