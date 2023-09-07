package com.yolt.providers.openbanking.ais.generic2.oauth2.implementations;

import com.yolt.providers.openbanking.ais.common.HttpUtils;
import com.yolt.providers.openbanking.ais.generic2.DefaultProperties;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.oauth2.BasicOauthClient;
import com.yolt.providers.openbanking.ais.generic2.oauth2.tokenbodysupplier.BasicOauthTokenBodyProducer;

import java.util.function.Function;

public class DefaultClientSecretBasicOauth2Client extends BasicOauthClient {

    public DefaultClientSecretBasicOauth2Client(final DefaultProperties properties, boolean isInPisFlow) {
        super(properties.getOAuthTokenUrl(),
                (Function<DefaultAuthMeans, String>) authenticationMeans -> HttpUtils.basicCredentials(authenticationMeans.getClientId(), authenticationMeans.getClientSecret()),
                new BasicOauthTokenBodyProducer(), isInPisFlow);
    }
}
