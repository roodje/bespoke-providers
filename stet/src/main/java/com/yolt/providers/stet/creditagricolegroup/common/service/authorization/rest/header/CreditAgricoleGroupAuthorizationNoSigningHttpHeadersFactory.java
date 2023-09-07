package com.yolt.providers.stet.creditagricolegroup.common.service.authorization.rest.header;

import com.yolt.providers.common.rest.tracing.ExternalTracingUtil;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.http.headers.HttpHeadersBuilder;
import com.yolt.providers.stet.generic.service.authorization.request.TokenRequest;
import com.yolt.providers.stet.generic.service.authorization.rest.header.DefaultAuthorizationNoSigningHttpHeadersFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;

@RequiredArgsConstructor
public class CreditAgricoleGroupAuthorizationNoSigningHttpHeadersFactory extends DefaultAuthorizationNoSigningHttpHeadersFactory {

    private static final String CORRELATION_ID_HEADER = "correlationid";
    private static final String CATS_CONSOMMATEUR_HEADER = "cats_consommateur";
    private static final String CATS_CONSOMMATEUR_ORIGINE_HEADER = "cats_consommateurorigine";
    private static final String CATS_CANAL_HEADER = "cats_canal";

    private static final String CONSOMMATEUR_VALUE = "{\"consommateur\":{\"nom\":\"Yolt\",\"version\":\"1.0.0\"}}";
    private static final String CATS_CANAL_VALUE = "{\"canal\":{\"canalId\":\"internet\",\"canalDistribution\":\"internet\"}}";

    @Override
    public HttpHeaders createAccessTokenHeaders(DefaultAuthenticationMeans authMeans, Object body, TokenRequest request) {
        return HttpHeadersBuilder.enhancing(super.createAccessTokenHeaders(authMeans, body, request))
                .withCustomHeader(CORRELATION_ID_HEADER, ExternalTracingUtil.createLastExternalTraceId())
                .withCustomHeader(CATS_CONSOMMATEUR_HEADER, CONSOMMATEUR_VALUE)
                .withCustomHeader(CATS_CONSOMMATEUR_ORIGINE_HEADER, CONSOMMATEUR_VALUE)
                .withCustomHeader(CATS_CANAL_HEADER, CATS_CANAL_VALUE)
                .build();
    }
}