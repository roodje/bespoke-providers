package com.yolt.providers.stet.cicgroup.common.service.registration.rest.header;

import com.yolt.providers.common.rest.tracing.ExternalTracingUtil;
import com.yolt.providers.stet.generic.http.headers.HttpHeadersBuilder;
import com.yolt.providers.stet.generic.service.registration.request.RegistrationRequest;
import com.yolt.providers.stet.generic.service.registration.rest.header.RegistrationHttpHeadersFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

@RequiredArgsConstructor
public class CicGroupGroupRegistrationHttpHeadersFactory implements RegistrationHttpHeadersFactory {

    @Override
    public HttpHeaders createRegistrationHttpHeaders(RegistrationRequest registerRequest, Object body, HttpMethod httpMethod, String url) {
        return HttpHeadersBuilder.builder()
                .withContentType(MediaType.APPLICATION_JSON)
                .withCustomXRequestId(ExternalTracingUtil.createLastExternalTraceId())
                .build();
    }
}
