package com.yolt.providers.starlingbank.common.http.authorizationurlparametersproducer;

import com.yolt.providers.starlingbank.common.auth.StarlingBankAuthenticationMeans;
import org.springframework.util.MultiValueMap;

import static com.yolt.providers.common.constants.OAuth.SCOPE;

public class StarlingBankPisAuthorizationUrlParametersProducer extends StarlingBankAisAuthorizationUrlParametersProducer {

    private static final String PAYMENT_SCOPE = "account:read%20account-list:read%20pay-local-once:create%20pay-local:read";

    @Override
    public MultiValueMap<String, String> createAuthorizationUrlParameters(String redirectUrl, String loginState, StarlingBankAuthenticationMeans authMeans) {
        MultiValueMap<String, String> params = super.createBasicAuthorizationUrlParameters(redirectUrl, loginState, authMeans);
        params.add(SCOPE, PAYMENT_SCOPE);

        return params;
    }
}
