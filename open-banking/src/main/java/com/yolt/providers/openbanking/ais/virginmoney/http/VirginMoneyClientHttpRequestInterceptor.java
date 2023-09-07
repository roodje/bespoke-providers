package com.yolt.providers.openbanking.ais.virginmoney.http;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

public class VirginMoneyClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {

    private static final String ACCOUNT_ACCESS_CONSENTS_PATH = "/aisp/account-access-consents";

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        if (HttpMethod.DELETE.equals(request.getMethod()) && request.getURI().getPath().contains(ACCOUNT_ACCESS_CONSENTS_PATH)) {
            request.getHeaders().remove(HttpHeaders.ACCEPT);
        }
        return execution.execute(request, body);
    }
}
