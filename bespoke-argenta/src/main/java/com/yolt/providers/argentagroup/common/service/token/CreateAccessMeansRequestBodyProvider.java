package com.yolt.providers.argentagroup.common.service.token;

import com.yolt.providers.argentagroup.common.service.DefaultAuthenticationMeans;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public class CreateAccessMeansRequestBodyProvider {

    private static final String CLIENT_ID_PARAMETER_NAME = "client_id";
    private static final String CODE_PARAMETER_NAME = "code";
    private static final String CODE_VERIFIER_PARAMETER_NAME = "code_verifier";
    private static final String GRANT_TYPE_PARAMETER_NAME = "grant_type";
    private static final String REDIRECT_URI_PARAMETER_NAME = "redirect_uri";
    private static final String AUTHORIZATION_CODE_GRANT_TYPE = "authorization_code";

    public MultiValueMap<String, String> provideRequestBody(final DefaultAuthenticationMeans authenticationMeans,
                                                            final String authorizationCode,
                                                            final String codeVerifier,
                                                            final String redirectUrl) {
        MultiValueMap<String, String> bodyMap = new LinkedMultiValueMap<>();
        bodyMap.set(CLIENT_ID_PARAMETER_NAME, authenticationMeans.getClientId());
        bodyMap.set(CODE_PARAMETER_NAME, authorizationCode);
        bodyMap.set(CODE_VERIFIER_PARAMETER_NAME, codeVerifier);
        bodyMap.set(GRANT_TYPE_PARAMETER_NAME, AUTHORIZATION_CODE_GRANT_TYPE);
        bodyMap.set(REDIRECT_URI_PARAMETER_NAME, redirectUrl);

        return bodyMap;
    }

}
