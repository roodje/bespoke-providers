package com.yolt.providers.openbanking.ais.vanquisgroup.common.ouath2;

import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.oauth2.tokenbodysupplier.PrivateKeyJwtTokenBodyProducer;
import org.springframework.util.MultiValueMap;

import java.util.List;

import static com.yolt.providers.common.constants.OAuth.SCOPE;

public class VanquisAccessTokenBodyProducer extends PrivateKeyJwtTokenBodyProducer {

    private static final String ACCESS_TOKEN_SCOPE = "accounts offline_access";

    @Override
    public MultiValueMap<String, String> getCreateAccessTokenBody(final DefaultAuthMeans authenticationMeans, final String authorizationCode, final String redirectURI, final String... args) {
        MultiValueMap<String, String> body = super.getCreateAccessTokenBody(authenticationMeans, authorizationCode, redirectURI, args);
        body.replace(SCOPE, List.of(ACCESS_TOKEN_SCOPE));
        return body;
    }
}
