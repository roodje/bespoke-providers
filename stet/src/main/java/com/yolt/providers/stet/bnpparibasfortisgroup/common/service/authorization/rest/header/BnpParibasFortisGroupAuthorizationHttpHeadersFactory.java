package com.yolt.providers.stet.bnpparibasfortisgroup.common.service.authorization.rest.header;

import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.http.headers.HttpHeadersBuilder;
import com.yolt.providers.stet.generic.service.authorization.request.TokenRequest;
import com.yolt.providers.stet.generic.service.authorization.rest.header.AuthorizationHttpHeadersFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public class BnpParibasFortisGroupAuthorizationHttpHeadersFactory implements AuthorizationHttpHeadersFactory {

    private static final String X_OPENBANK_STET_VERSION_HEADER = "x-openbank-stet-version";
    private static final String X_OPENBANK_STET_VERSION_VALUE = "1.4.0.47.develop";

    @Override
    public HttpHeaders createAccessTokenHeaders(DefaultAuthenticationMeans authMeans, Object body, TokenRequest request) {
        return HttpHeadersBuilder.builder()
                .withContentType(MediaType.APPLICATION_FORM_URLENCODED)
                .withCustomHeader(X_OPENBANK_STET_VERSION_HEADER, X_OPENBANK_STET_VERSION_VALUE)
                .build();
    }
}
