package com.yolt.providers.openbanking.ais.revolutgroup.common.oauth2.tokenbodysupplier;

import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import com.yolt.providers.openbanking.ais.generic2.oauth2.tokenbodysupplier.BasicOauthTokenBodyProducer;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static com.yolt.providers.common.constants.OAuth.*;

public class RevolutBasicOauthTokenBodyProducer extends BasicOauthTokenBodyProducer {
    @Override
    public MultiValueMap<String, String> getCreateClientCredentialsBody(DefaultAuthMeans authenticationMeans, TokenScope scope,
                                                                        String... args) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add(GRANT_TYPE, "client_credentials");
        body.add(SCOPE, scope.getGrantScope());
        body.add(CLIENT_ID, authenticationMeans.getClientId());
        return body;

    }
}
