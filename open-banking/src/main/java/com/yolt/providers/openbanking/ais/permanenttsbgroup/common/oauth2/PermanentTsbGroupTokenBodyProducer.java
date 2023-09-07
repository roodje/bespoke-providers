package com.yolt.providers.openbanking.ais.permanenttsbgroup.common.oauth2;

import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import com.yolt.providers.openbanking.ais.generic2.oauth2.tokenbodysupplier.BasicOauthTokenBodyProducer;
import org.springframework.util.MultiValueMap;

import static com.yolt.providers.common.constants.OAuth.CLIENT_ASSERTION;
import static com.yolt.providers.common.constants.OAuth.SCOPE;

public class PermanentTsbGroupTokenBodyProducer extends BasicOauthTokenBodyProducer {

    private final TokenScope scope;

    public PermanentTsbGroupTokenBodyProducer(TokenScope scope) {
        this.scope = scope;
    }

    @Override
    public MultiValueMap<String, String> getCreateClientCredentialsBody(DefaultAuthMeans authenticationMeans, TokenScope scope,
                                                                        String... args) {
        MultiValueMap<String, String> body = super.getCreateClientCredentialsBody(authenticationMeans,
                scope,
                args);
        String clientAssertion = args[0];
        body.add(CLIENT_ASSERTION, clientAssertion);
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