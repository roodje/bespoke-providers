package com.yolt.providers.stet.generic.service.fetchdata.rest.header;

import com.yolt.providers.common.rest.tracing.ExternalTracingUtil;
import com.yolt.providers.stet.generic.service.fetchdata.request.DataRequest;
import com.yolt.providers.stet.generic.http.headers.HttpHeadersBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import java.util.Collections;
import java.util.function.Supplier;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@RequiredArgsConstructor
public class FetchDataNoSigningHttpHeadersFactory implements FetchDataHttpHeadersFactory {

    private final Supplier<String> lastExternalTraceIdSupplier;

    public FetchDataNoSigningHttpHeadersFactory() {
        this.lastExternalTraceIdSupplier = ExternalTracingUtil::createLastExternalTraceId;
    }

    @Override
    public HttpHeaders createFetchDataHeaders(String endpoint, DataRequest dataRequest, HttpMethod method) {
        return prepareCommonHttpHeaders(dataRequest.getAccessToken(), dataRequest.getPsuIpAddress());
    }

    protected HttpHeaders prepareCommonHttpHeaders(String accessToken,
                                                   String psuIpAddress) {
        return HttpHeadersBuilder.builder()
                .withAccept(Collections.singletonList(APPLICATION_JSON))
                .withBearerAuthorization(accessToken)
                .withContentType(APPLICATION_JSON)
                .withPsuIpAddress(psuIpAddress)
                .withCustomXRequestId(lastExternalTraceIdSupplier)
                .build();
    }
}
