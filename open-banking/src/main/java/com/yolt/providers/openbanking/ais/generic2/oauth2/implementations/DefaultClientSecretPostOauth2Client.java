package com.yolt.providers.openbanking.ais.generic2.oauth2.implementations;

import com.yolt.providers.openbanking.ais.common.HttpUtils;
import com.yolt.providers.openbanking.ais.generic2.DefaultProperties;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.oauth2.BasicOauthClient;
import com.yolt.providers.openbanking.ais.generic2.oauth2.tokenbodysupplier.PostSecretTokenBodyProducer;

import java.util.function.Function;

public class DefaultClientSecretPostOauth2Client extends BasicOauthClient {

    public DefaultClientSecretPostOauth2Client(final DefaultProperties properties,
                                               boolean isInPisFlow) {
        super(properties.getOAuthTokenUrl(),
                any -> null,
                any -> null,
                (Function<DefaultAuthMeans, String>) authMeans -> HttpUtils.basicCredentials(authMeans.getClientId(), authMeans.getClientSecret()),
                new PostSecretTokenBodyProducer(),
                isInPisFlow);
    }
}