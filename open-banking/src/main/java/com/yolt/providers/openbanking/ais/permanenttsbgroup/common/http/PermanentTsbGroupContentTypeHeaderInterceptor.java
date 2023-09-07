package com.yolt.providers.openbanking.ais.permanenttsbgroup.common.http;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

@RequiredArgsConstructor
public class PermanentTsbGroupContentTypeHeaderInterceptor implements ClientHttpRequestInterceptor {

    private final String registrationUrl;

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        if (registrationUrl.equals(request.getURI().toString())) {
            request.getHeaders().remove(HttpHeaders.CONTENT_TYPE);
            String rawBody = new String(body).substring(1, body.length - 1);
            return execution.execute(request, rawBody.getBytes());
        }
        return execution.execute(request, body);
    }
}
