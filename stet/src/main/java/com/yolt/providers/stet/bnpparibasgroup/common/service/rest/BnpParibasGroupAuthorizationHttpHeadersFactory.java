package com.yolt.providers.stet.bnpparibasgroup.common.service.rest;

import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.http.headers.HttpHeadersBuilder;
import com.yolt.providers.stet.generic.service.authorization.request.TokenRequest;
import com.yolt.providers.stet.generic.service.authorization.rest.header.AuthorizationHttpHeadersFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.Collections;

import static org.springframework.http.MediaType.APPLICATION_JSON;
public class BnpParibasGroupAuthorizationHttpHeadersFactory implements AuthorizationHttpHeadersFactory {

    @Override
    public HttpHeaders createAccessTokenHeaders(DefaultAuthenticationMeans authMeans, Object body, TokenRequest request) {
        return HttpHeadersBuilder.builder()
                .withBasicAuthorization(authMeans.getClientId(), authMeans.getClientSecret())
                .withContentType(MediaType.APPLICATION_FORM_URLENCODED)
                .withAccept(Collections.singletonList(APPLICATION_JSON))
                .build();
    }
}
