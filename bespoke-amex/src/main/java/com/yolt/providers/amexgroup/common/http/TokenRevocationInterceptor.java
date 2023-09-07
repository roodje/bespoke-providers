package com.yolt.providers.amexgroup.common.http;

import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

public class TokenRevocationInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(final HttpRequest request, final byte[] body, final ClientHttpRequestExecution execution) throws IOException {
        if (request.getURI().toString().contains("token_revocation")) {
            request.getHeaders().setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        }
        return execution.execute(request, body);
    }
}
