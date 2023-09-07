package com.yolt.providers.stet.cicgroup.common.service.authorization.rest.header;

import com.yolt.providers.common.rest.tracing.ExternalTracingUtil;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.domain.HttpHeadersExtension;
import com.yolt.providers.stet.generic.http.headers.HttpHeadersBuilder;
import com.yolt.providers.stet.generic.service.authorization.request.TokenRequest;
import com.yolt.providers.stet.generic.service.authorization.rest.header.DefaultAuthorizationNoSigningHttpHeadersFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;

@RequiredArgsConstructor
public class CicGroupAuthorizationNoSigningHttpHeadersFactory extends DefaultAuthorizationNoSigningHttpHeadersFactory {

    @Override
    public HttpHeaders createAccessTokenHeaders(DefaultAuthenticationMeans authMeans, Object body, TokenRequest request) {
        return HttpHeadersBuilder.enhancing(super.createAccessTokenHeaders(authMeans, body, request))
                .withCustomHeader(HttpHeadersExtension.X_REQUEST_ID, ExternalTracingUtil.createLastExternalTraceId())
                .build();
    }
}
