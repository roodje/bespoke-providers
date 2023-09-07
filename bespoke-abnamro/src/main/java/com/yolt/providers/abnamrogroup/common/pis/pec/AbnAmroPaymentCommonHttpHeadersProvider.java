package com.yolt.providers.abnamrogroup.common.pis.pec;

import com.yolt.providers.abnamrogroup.common.pis.AbnAmroXRequestIdHeaderProvider;
import com.yolt.providers.common.rest.tracing.ExternalTracingUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.Collections;

@RequiredArgsConstructor
public class AbnAmroPaymentCommonHttpHeadersProvider {

    private static final String API_KEY_HEADER = "API-Key";
    private static final String X_REQUEST_ID_HEADER = "X-Request-ID";

    private final AbnAmroXRequestIdHeaderProvider xRequestIdHeaderProvider;

    public HttpHeaders provideCommonHttpHeaders(String accessToken, String apiKey) {
        var httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.setBearerAuth(accessToken);
        httpHeaders.add(API_KEY_HEADER, apiKey);
        httpHeaders.add(X_REQUEST_ID_HEADER, ExternalTracingUtil.createLastExternalTraceId(xRequestIdHeaderProvider::provideXRequestIdHeader));
        return httpHeaders;
    }
}
