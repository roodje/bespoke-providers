package com.yolt.providers.argentagroup.common.service.token;

import com.yolt.providers.argentagroup.common.service.DefaultAuthenticationMeans;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public class RefreshAccessMeansRequestBodyProvider {

    private static final String GRANT_TYPE_PARAMETER_NAME = "grant_type";
    private static final String REFRESH_TOKEN_PARAMETER_NAME = "refresh_token";


    public MultiValueMap<String, String> provideRequestBody(final DefaultAuthenticationMeans authenticationMeans,
                                                            final AccessMeans accessMeans) {
        MultiValueMap<String, String> bodyMap = new LinkedMultiValueMap<>();
        bodyMap.set(GRANT_TYPE_PARAMETER_NAME, REFRESH_TOKEN_PARAMETER_NAME);
        bodyMap.set(REFRESH_TOKEN_PARAMETER_NAME, accessMeans.getRefreshToken());

        return bodyMap;
    }
}

