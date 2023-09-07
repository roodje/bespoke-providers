package com.yolt.providers.openbanking.ais.generic2.oauth2.implementations;

import com.yolt.providers.openbanking.ais.generic2.DefaultProperties;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.oauth2.BasicOauthClient;
import com.yolt.providers.openbanking.ais.generic2.oauth2.tokenbodysupplier.BasicOauthTokenBodyProducer;
import com.yolt.providers.openbanking.ais.generic2.oauth2.tokenbodysupplier.TokenRequestBodyProducer;
import org.bouncycastle.util.encoders.Base64;

import java.util.function.Function;

import static java.nio.charset.StandardCharsets.UTF_8;

public class DefaultMutualTlsOauth2Client extends BasicOauthClient {

    public DefaultMutualTlsOauth2Client(final DefaultProperties properties, boolean isInPisFlow) {
        super(properties.getOAuthTokenUrl(),
                (Function< DefaultAuthMeans, String>) authenticationMeans -> "Basic " + new String(Base64.encode(authenticationMeans.getClientId().getBytes(UTF_8))),
                new BasicOauthTokenBodyProducer(), isInPisFlow);
    }

    public DefaultMutualTlsOauth2Client(final DefaultProperties properties, final TokenRequestBodyProducer requestBodyProducer, boolean isInPisFlow) {
        super(properties.getOAuthTokenUrl(),
                (Function<DefaultAuthMeans, String>) authenticationMeans -> "Basic " + new String(Base64.encode(authenticationMeans.getClientId().getBytes(UTF_8))),
                requestBodyProducer, isInPisFlow);
    }

    public DefaultMutualTlsOauth2Client(final DefaultProperties properties,
                                        final Function<DefaultAuthMeans, String> authenticationHeaderSupplier,
                                        final TokenRequestBodyProducer requestBodyProducer,
                                        boolean isInPisFlow) {
        super(properties.getOAuthTokenUrl(),
                authenticationHeaderSupplier,
                requestBodyProducer, isInPisFlow);
    }
}
