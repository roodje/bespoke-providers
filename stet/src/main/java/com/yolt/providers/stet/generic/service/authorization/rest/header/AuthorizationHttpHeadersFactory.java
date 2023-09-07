package com.yolt.providers.stet.generic.service.authorization.rest.header;

import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.service.authorization.request.TokenRequest;
import org.springframework.http.HttpHeaders;

public interface AuthorizationHttpHeadersFactory {

    HttpHeaders createAccessTokenHeaders(DefaultAuthenticationMeans authMeans, Object body, TokenRequest request);
}
