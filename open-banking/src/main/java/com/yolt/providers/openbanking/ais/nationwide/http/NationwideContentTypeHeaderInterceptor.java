package com.yolt.providers.openbanking.ais.nationwide.http;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

@RequiredArgsConstructor
public class NationwideContentTypeHeaderInterceptor implements ClientHttpRequestInterceptor {

    private final String oAuthTokenUrl;

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        if (oAuthTokenUrl.equals(request.getURI().toString())) {
            request.getHeaders().setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        } else if (request.getMethod() == HttpMethod.POST && request.getURI().toString().contains("payment")) {
            request.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        }
        return execution.execute(request, body);
    }
}
