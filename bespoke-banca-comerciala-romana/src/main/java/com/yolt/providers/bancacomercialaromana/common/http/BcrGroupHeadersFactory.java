package com.yolt.providers.bancacomercialaromana.common.http;

import com.yolt.providers.common.rest.tracing.ExternalTracingUtil;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;

import java.util.Collections;

import static java.util.Collections.singletonList;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@AllArgsConstructor
public class BcrGroupHeadersFactory {

    private static final String X_REQUEST_ID_HEADER = "X-Request-ID";
    private static final String WEB_API_KEY_HEADER = "web-api-key";

    HttpHeaders getRequestHeaders(String accessToken, String webApiKey) {
        HttpHeaders headers = new HttpHeaders();

        headers.set(AUTHORIZATION, "Bearer " + accessToken);
        headers.set(WEB_API_KEY_HEADER, webApiKey);
        headers.setContentType(APPLICATION_JSON);
        headers.setAccept(singletonList(APPLICATION_JSON));

        final String lastExternalTraceId = ExternalTracingUtil.createLastExternalTraceId();
        headers.set(X_REQUEST_ID_HEADER, lastExternalTraceId);
        return headers;
    }

    HttpHeaders getTokenHeaders(String clientId, String clientSecret) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_FORM_URLENCODED);
        headers.setAccept(Collections.singletonList(APPLICATION_JSON));
        headers.setBasicAuth(clientId, clientSecret);
        return headers;
    }
}
