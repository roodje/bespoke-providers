package com.yolt.providers.stet.labanquepostalegroup.common.service.authorization.rest.header;

import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.http.headers.HttpHeadersBuilder;
import com.yolt.providers.stet.generic.service.authorization.request.TokenRequest;
import com.yolt.providers.stet.generic.service.authorization.rest.header.DefaultAuthorizationNoSigningHttpHeadersFactory;
import org.springframework.http.HttpHeaders;

public class LaBanquePostaleGroupAuthorizationNoSigningHttpHeadersFactory extends DefaultAuthorizationNoSigningHttpHeadersFactory {

    @Override
    public HttpHeaders createAccessTokenHeaders(DefaultAuthenticationMeans authMeans, Object body, TokenRequest accessTokenRequest) {
        return HttpHeadersBuilder.enhancing(super.createAccessTokenHeaders(authMeans, body, accessTokenRequest))
                .withBasicAuthorization(authMeans.getClientId(), authMeans.getClientSecret())
                .build();
    }
}
