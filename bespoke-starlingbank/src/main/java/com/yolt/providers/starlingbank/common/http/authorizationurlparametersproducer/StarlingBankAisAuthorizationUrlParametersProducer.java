package com.yolt.providers.starlingbank.common.http.authorizationurlparametersproducer;

import com.yolt.providers.starlingbank.common.auth.StarlingBankAuthenticationMeans;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static com.yolt.providers.common.constants.OAuth.*;

public class StarlingBankAisAuthorizationUrlParametersProducer implements StarlingBankAuthorizationUrlParametersProducer {

    private static final String ACCOUNTS_SCOPE = "account:read%20account-holder-name:read%20account-holder-type:read%20account-identifier:read%20account-list:read%20balance:read%20savings-goal:read%20savings-goal-transfer:read%20transaction:read";

    @Override
    public MultiValueMap<String, String> createAuthorizationUrlParameters(String redirectUrl, String loginState, StarlingBankAuthenticationMeans authMeans) {
        MultiValueMap<String, String> params = createBasicAuthorizationUrlParameters(redirectUrl, loginState, authMeans);
        params.add(SCOPE, ACCOUNTS_SCOPE);

        return params;
    }

    protected MultiValueMap<String, String> createBasicAuthorizationUrlParameters(String redirectUrl, String loginState, StarlingBankAuthenticationMeans authMeans) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add(RESPONSE_TYPE, CODE);
        params.add(CLIENT_ID, authMeans.getApiKey());
        params.add(REDIRECT_URI, redirectUrl);
        params.add(STATE, loginState);

        return params;
    }
}
