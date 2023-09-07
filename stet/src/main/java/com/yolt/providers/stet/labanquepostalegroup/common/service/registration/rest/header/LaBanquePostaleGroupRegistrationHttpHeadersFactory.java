package com.yolt.providers.stet.labanquepostalegroup.common.service.registration.rest.header;

import com.yolt.providers.common.rest.tracing.ExternalTracingUtil;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.service.registration.request.RegistrationRequest;
import com.yolt.providers.stet.generic.http.headers.HttpHeadersBuilder;
import com.yolt.providers.stet.generic.service.registration.rest.header.RegistrationHttpHeadersFactory;
import com.yolt.providers.stet.labanquepostalegroup.labanquepostale.auth.LaBanquePostaleAuthenticationMeans;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import java.util.Collections;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class LaBanquePostaleGroupRegistrationHttpHeadersFactory implements RegistrationHttpHeadersFactory {

    private final Supplier<String> lastExternalTraceIdSupplier;

    public LaBanquePostaleGroupRegistrationHttpHeadersFactory() {
        this.lastExternalTraceIdSupplier = ExternalTracingUtil::createLastExternalTraceId;
    }

    @Override
    public HttpHeaders createRegistrationHttpHeaders(RegistrationRequest request,
                                                     Object body,
                                                     HttpMethod method,
                                                     String url) {
        return prepareCommonHttpHeaders(request.getAuthMeans());
    }

    private HttpHeaders prepareCommonHttpHeaders(DefaultAuthenticationMeans defaultAuthMeans) {
        LaBanquePostaleAuthenticationMeans authMeans = (LaBanquePostaleAuthenticationMeans) defaultAuthMeans;
        return HttpHeadersBuilder.builder()
                .withContentType(MediaType.APPLICATION_JSON)
                .withAccept(Collections.singletonList(MediaType.APPLICATION_JSON))
                .withCustomXRequestId(lastExternalTraceIdSupplier)
                .withBasicAuthorization(authMeans.getPortalUsername(), authMeans.getPortalPassword())
                .build();
    }
}
