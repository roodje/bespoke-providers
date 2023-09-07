package com.yolt.providers.argentagroup.common.service.token;

import com.yolt.providers.argentagroup.common.service.DefaultAuthenticationMeans;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public class AccessMeansHttpHeadersProvider {

    private static final String API_KEY_HEADER_NAME = "apiKey";

    public HttpHeaders provideRequestHeaders(final DefaultAuthenticationMeans authenticationMeans) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set(API_KEY_HEADER_NAME, authenticationMeans.getApiKey());

        return headers;
    }
}
