package com.yolt.providers.openbanking.ais.generic2.oauth2.tokenbodysupplier;

import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import lombok.AllArgsConstructor;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static com.yolt.providers.common.constants.OAuth.*;

@AllArgsConstructor
public class PrivateKeyJwtTokenBodyProducer implements TokenRequestBodyProducer<MultiValueMap<String, String>> {

    public static final String URN_IETF_PARAMS_OAUTH_CLIENT_ASSERTION_TYPE_JWT_BEARER = "urn:ietf:params:oauth:client-assertion-type:jwt-bearer";

    @Override
    public MultiValueMap<String, String> getRefreshAccessTokenBody(DefaultAuthMeans authenticationMeans,
                                                                   String refreshToken,
                                                                   String... args) {
        String redirectURI = args[0];
        String grantScope = args[1];
        String clientAssertion = args[2];

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add(GRANT_TYPE, "refresh_token");
        body.add(REFRESH_TOKEN, refreshToken);
        body.add(REDIRECT_URI, redirectURI);
        body.add(SCOPE, grantScope);
        body.add(CLIENT_ASSERTION_TYPE, URN_IETF_PARAMS_OAUTH_CLIENT_ASSERTION_TYPE_JWT_BEARER);
        body.add(CLIENT_ASSERTION, clientAssertion);
        return body;
    }

    @Override
    public MultiValueMap<String, String> getCreateAccessTokenBody(DefaultAuthMeans authenticationMeans,
                                                                  String authorizationCode,
                                                                  String redirectURI,
                                                                  String... args) {

        String grantScope = args[0];
        String clientAssertion = args[1];

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add(GRANT_TYPE, "authorization_code");
        body.add(REDIRECT_URI, redirectURI);
        body.add(SCOPE, grantScope);
        body.add(CLIENT_ASSERTION_TYPE, URN_IETF_PARAMS_OAUTH_CLIENT_ASSERTION_TYPE_JWT_BEARER);
        body.add(CLIENT_ASSERTION, clientAssertion);
        body.add(CODE, authorizationCode);
        return body;
    }

    @Override
    public MultiValueMap<String, String> getCreateClientCredentialsBody(DefaultAuthMeans authenticationMeans,
                                                                        TokenScope scope,
                                                                        String... args) {

        String clientAssertion = args[0];

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add(GRANT_TYPE, "client_credentials");
        body.add(SCOPE, scope.getGrantScope());
        body.add(CLIENT_ASSERTION_TYPE, URN_IETF_PARAMS_OAUTH_CLIENT_ASSERTION_TYPE_JWT_BEARER);
        body.add(CLIENT_ASSERTION, clientAssertion);
        return body;
    }
}
