package com.yolt.providers.stet.generic.service.authorization.rest.header;

import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.http.headers.HttpHeadersBuilder;
import com.yolt.providers.stet.generic.service.authorization.request.TokenRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.Collections;

public class DefaultAuthorizationSigningHttpHeadersFactory implements AuthorizationHttpHeadersFactory {

    @Override
    public HttpHeaders createAccessTokenHeaders(DefaultAuthenticationMeans authMeans, Object body, TokenRequest request) {
        return HttpHeadersBuilder.builder()
                .withContentType(MediaType.APPLICATION_FORM_URLENCODED)
                .withAccept(Collections.singletonList(MediaType.APPLICATION_JSON))
                .build();
    }
}
